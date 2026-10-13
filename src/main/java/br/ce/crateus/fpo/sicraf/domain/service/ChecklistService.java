package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.ChecklistRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.ChecklistResponse;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.Checklist;
import br.ce.crateus.fpo.sicraf.domain.model.ChecklistItem;
import br.ce.crateus.fpo.sicraf.domain.model.Motorista;
import br.ce.crateus.fpo.sicraf.domain.model.Veiculo;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.ChecklistRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.MotoristaRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ChecklistRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final MotoristaRepository motoristaRepository;

    public Page<ChecklistResponse> listar(String status, Long veiculoId, Long motoristaId, Pageable pageable) {
        return repository.findByFilters(status, veiculoId, motoristaId, pageable)
                .map(this::toResponse);
    }

    public ChecklistResponse buscarPorId(Long id) {
        Checklist checklist = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Check-list não encontrado: id=" + id));
        return toResponse(checklist);
    }

    public List<ChecklistResponse> listarPorVeiculo(Long veiculoId) {
        return repository.findByVeiculoIdOrderByDataSaidaDesc(veiculoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ChecklistResponse> listarPorMotorista(Long motoristaId) {
        return repository.findByMotoristaIdOrderByDataSaidaDesc(motoristaId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChecklistResponse criar(ChecklistRequest request) {
        // 1. Buscar veículo e motorista
        Veiculo veiculo = veiculoRepository.findById(request.getVeiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Veículo não encontrado: id=" + request.getVeiculoId()));

        Motorista motorista = motoristaRepository.findById(request.getMotoristaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Motorista não encontrado: id=" + request.getMotoristaId()));

        // ============================================
        // REGRAS DE NEGÓCIO (RN01 do SICRAF)
        // ============================================

        // RN01-a: Veículo precisa estar ATIVO
        if (!"ATIVO".equalsIgnoreCase(veiculo.getStatus())) {
            throw new BusinessException(
                    "Veículo " + veiculo.getPlaca() + " não está ATIVO (status atual: "
                            + veiculo.getStatus() + ")");
        }

        // RN01-b: Motorista precisa estar ATIVO
        if (!"ATIVO".equalsIgnoreCase(motorista.getStatus())) {
            throw new BusinessException(
                    "Motorista " + motorista.getNome() + " não está ATIVO (status atual: "
                            + motorista.getStatus() + ")");
        }

        // RN01-c: CNH do motorista não pode estar vencida
        if (motorista.getValidadeCnh() != null
                && motorista.getValidadeCnh().isBefore(LocalDate.now())) {
            throw new BusinessException(
                    "CNH do motorista " + motorista.getNome() + " está vencida ("
                            + motorista.getValidadeCnh() + "). Saída bloqueada.");
        }

        // RN01-d: Não pode haver checklist EM_ANDAMENTO para o mesmo veículo
        if (repository.existeChecklistAbertoParaVeiculo(veiculo.getId())) {
            throw new BusinessException(
                    "Já existe um check-list em andamento para o veículo "
                            + veiculo.getPlaca() + ". Conclua ou cancele antes de abrir um novo.");
        }

        // ============================================
        // Criação do checklist
        // ============================================
        Checklist checklist = Checklist.builder()
                .veiculo(veiculo)
                .motorista(motorista)
                .kmSaida(request.getKmSaida() != null ? request.getKmSaida() : veiculo.getKmAtual())
                .observacoes(request.getObservacoes())
                .status("EM_ANDAMENTO")
                .build();

        // Se o request não trouxe itens, cria todos os itens padrão como NÃO verificados
        Map<String, ChecklistRequest.ItemRequest> itensEnviados = request.getItens() == null
                ? Map.of()
                : request.getItens().stream()
                .collect(Collectors.toMap(
                        ChecklistRequest.ItemRequest::getItem,
                        Function.identity(),
                        (a, b) -> a));

        for (String nomeItem : ChecklistItem.ITENS_PADRAO) {
            ChecklistRequest.ItemRequest enviado = itensEnviados.get(nomeItem);

            ChecklistItem item = ChecklistItem.builder()
                    .item(nomeItem)
                    .verificado(enviado != null && enviado.isVerificado())
                    .observacao(enviado != null ? enviado.getObservacao() : null)
                    .build();

            checklist.adicionarItem(item);
        }

        // Se todos os itens já vierem verificados, conclui automaticamente
        if (checklist.isTodosItensVerificados()) {
            checklist.setStatus("CONCLUIDO");
        }

        Checklist salvo = repository.save(checklist);

        // Atualiza KM do veículo se o KM informado for maior que o atual
        if (salvo.getKmSaida() != null
                && (veiculo.getKmAtual() == null
                || salvo.getKmSaida().compareTo(veiculo.getKmAtual()) > 0)) {
            veiculo.setKmAtual(salvo.getKmSaida());
            veiculoRepository.save(veiculo);
        }

        return toResponse(salvo);
    }

    @Transactional
    public ChecklistResponse concluir(Long id) {
        Checklist checklist = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Check-list não encontrado: id=" + id));

        if (!"EM_ANDAMENTO".equalsIgnoreCase(checklist.getStatus())) {
            throw new BusinessException(
                    "Só é possível concluir check-lists com status EM_ANDAMENTO (atual: "
                            + checklist.getStatus() + ")");
        }

        if (!checklist.isTodosItensVerificados()) {
            long pendentes = checklist.getItens().stream()
                    .filter(i -> !i.isVerificado())
                    .count();
            throw new BusinessException(
                    "Existem " + pendentes + " itens não verificados. Todos os itens devem ser marcados antes de concluir.");
        }

        checklist.setStatus("CONCLUIDO");
        return toResponse(repository.save(checklist));
    }

    @Transactional
    public ChecklistResponse cancelar(Long id) {
        Checklist checklist = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Check-list não encontrado: id=" + id));

        if ("CONCLUIDO".equalsIgnoreCase(checklist.getStatus())) {
            throw new BusinessException("Não é possível cancelar um check-list já concluído.");
        }

        checklist.setStatus("CANCELADO");
        return toResponse(repository.save(checklist));
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Check-list não encontrado: id=" + id);
        }
        repository.deleteById(id);
    }

    // ---------- Mapeamento ----------

    private ChecklistResponse toResponse(Checklist c) {
        ChecklistResponse r = new ChecklistResponse();
        r.setId(c.getId());
        r.setVeiculoId(c.getVeiculo().getId());
        r.setVeiculoPlaca(c.getVeiculo().getPlaca());
        r.setVeiculoModelo(c.getVeiculo().getModelo());
        r.setMotoristaId(c.getMotorista().getId());
        r.setMotoristaNome(c.getMotorista().getNome());
        r.setDataSaida(c.getDataSaida());
        r.setKmSaida(c.getKmSaida());
        r.setObservacoes(c.getObservacoes());
        r.setStatus(c.getStatus());
        r.setTodosVerificados(c.isTodosItensVerificados());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());

        r.setItens(c.getItens().stream().map(item -> {
            ChecklistResponse.ItemResponse ir = new ChecklistResponse.ItemResponse();
            ir.setId(item.getId());
            ir.setItem(item.getItem());
            ir.setVerificado(item.isVerificado());
            ir.setObservacao(item.getObservacao());
            return ir;
        }).toList());

        return r;
    }
}