package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.OrdemServicoRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.OrdemServicoResponse;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.*;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.OrdemServicoRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.TipoManutencaoRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private static final Set<String> STATUS_VALIDOS =
            Set.of("ABERTA", "EM_ANDAMENTO", "AGUARDANDO_PECAS", "CONCLUIDA", "CANCELADA");

    private static final Set<String> TIPOS_OFICINA =
            Set.of("INTERNA", "EXTERNA");

    private static final Set<String> TIPOS_ITEM =
            Set.of("PECA", "MAO_DE_OBRA", "SERVICO");

    private final OrdemServicoRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final TipoManutencaoRepository tipoManutencaoRepository;
    private final AlertaManutencaoService alertaService;

    public Page<OrdemServicoResponse> listar(String status, Long veiculoId, String tipoOficina, Pageable pageable) {
        return repository.findByFilters(status, veiculoId, tipoOficina, pageable)
                .map(this::toResponse);
    }

    public OrdemServicoResponse buscarPorId(Long id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço não encontrada: id=" + id));
        return toResponse(os);
    }

    public OrdemServicoResponse buscarPorNumero(String numeroOs) {
        OrdemServico os = repository.findByNumeroOs(numeroOs)
                .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada: número=" + numeroOs));
        return toResponse(os);
    }

    public List<OrdemServicoResponse> listarPorVeiculo(Long veiculoId) {
        return repository.findByVeiculoIdOrderByDataAberturaDesc(veiculoId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public OrdemServicoResponse criar(OrdemServicoRequest request) {
        Veiculo veiculo = veiculoRepository.findById(request.getVeiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Veículo não encontrado: id=" + request.getVeiculoId()));

        TipoManutencao tipo = null;
        if (request.getTipoManutencaoId() != null) {
            tipo = tipoManutencaoRepository.findById(request.getTipoManutencaoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Tipo de manutenção não encontrado: id=" + request.getTipoManutencaoId()));
        }

        // RN01: tipo de oficina válido
        String tipoOficina = request.getTipoOficina().toUpperCase().trim();
        if (!TIPOS_OFICINA.contains(tipoOficina)) {
            throw new BusinessException("Tipo de oficina inválido: " + request.getTipoOficina());
        }

        // RN02: oficina EXTERNA exige nome
        if ("EXTERNA".equals(tipoOficina)
                && (request.getOficinaNome() == null || request.getOficinaNome().isBlank())) {
            throw new BusinessException("Oficina EXTERNA exige o nome do fornecedor");
        }

        // RN03: gerar número sequencial
        String numeroOs = gerarNumeroOs();

        // RN04: itens obrigatórios
        if (request.getItens() == null || request.getItens().isEmpty()) {
            throw new BusinessException("Ordem de serviço exige pelo menos 1 item (peça ou mão de obra)");
        }

        OrdemServico os = OrdemServico.builder()
                .numeroOs(numeroOs)
                .veiculo(veiculo)
                .tipoManutencao(tipo)
                .kmAtual(request.getKmAtual() != null ? request.getKmAtual() : veiculo.getKmAtual())
                .tipoOficina(tipoOficina)
                .oficinaNome(request.getOficinaNome())
                .previsaoConclusao(request.getPrevisaoConclusao())
                .observacoes(request.getObservacoes())
                .status("ABERTA")
                .build();

        // Adicionar itens
        for (OrdemServicoRequest.ItemRequest itemReq : request.getItens()) {
            String tipoItem = itemReq.getTipoItem().toUpperCase().trim();
            if (!TIPOS_ITEM.contains(tipoItem)) {
                throw new BusinessException("Tipo de item inválido: " + itemReq.getTipoItem());
            }

            BigDecimal quantidade = itemReq.getQuantidade() != null
                    ? itemReq.getQuantidade() : BigDecimal.ONE;
            BigDecimal valorUnitario = itemReq.getValorUnitario() != null
                    ? itemReq.getValorUnitario() : BigDecimal.ZERO;

            OrdemServicoItem item = OrdemServicoItem.builder()
                    .descricao(itemReq.getDescricao())
                    .tipoItem(tipoItem)
                    .quantidade(quantidade)
                    .valorUnitario(valorUnitario)
                    .valorTotal(quantidade.multiply(valorUnitario))
                    .build();

            os.adicionarItem(item);
        }

        os.recalcularTotais();

        // RN05: bloquear criação de OS se já houver OS aberta para o veículo
        if (repository.existeOsAbertaParaVeiculo(veiculo.getId())) {
            throw new BusinessException(
                    "Já existe uma OS em aberto para o veículo " + veiculo.getPlaca()
                            + ". Conclua ou cancele antes de abrir uma nova.");
        }

        OrdemServico salvo = repository.save(os);

        // RN06: ao abrir OS, veículo vai para MANUTENCAO
        veiculo.setStatus("MANUTENCAO");
        veiculoRepository.save(veiculo);

        return toResponse(salvo);
    }

    @Transactional
    public OrdemServicoResponse atualizarStatus(Long id, String novoStatus) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada: id=" + id));

        String statusNormalizado = novoStatus.toUpperCase().trim();
        if (!STATUS_VALIDOS.contains(statusNormalizado)) {
            throw new BusinessException("Status inválido: " + novoStatus);
        }

        // RN07: OS CANCELADA ou CONCLUIDA não pode ser alterada
        if ("CANCELADA".equals(os.getStatus())) {
            throw new BusinessException("OS já está cancelada e não pode ser alterada");
        }
        if ("CONCLUIDA".equals(os.getStatus())) {
            throw new BusinessException("OS já está concluída e não pode ser alterada");
        }

        os.setStatus(statusNormalizado);

        // RN08: ao concluir, setar data de conclusão e devolver veículo para ATIVO
        if ("CONCLUIDA".equals(statusNormalizado)) {
            os.setDataConclusao(LocalDateTime.now());
            Veiculo veiculo = os.getVeiculo();
            veiculo.setStatus("ATIVO");

            // Atualiza KM do veículo se a OS tiver KM maior
            if (os.getKmAtual() != null
                    && (veiculo.getKmAtual() == null || os.getKmAtual().compareTo(veiculo.getKmAtual()) > 0)) {
                veiculo.setKmAtual(os.getKmAtual());
            }
            if (os.getTipoManutencao() != null) {
                alertaService.concluirPorVeiculoETipo(
                        veiculo.getId(), os.getTipoManutencao().getId());
            }
            veiculoRepository.save(veiculo);
        }

        // RN09: ao cancelar, devolver veículo para ATIVO
        if ("CANCELADA".equals(statusNormalizado)) {
            Veiculo veiculo = os.getVeiculo();
            veiculo.setStatus("ATIVO");
            veiculoRepository.save(veiculo);
        }

        return toResponse(repository.save(os));
    }

    @Transactional
    public OrdemServicoResponse concluir(Long id) {
        return atualizarStatus(id, "CONCLUIDA");
    }

    @Transactional
    public OrdemServicoResponse cancelar(Long id) {
        return atualizarStatus(id, "CANCELADA");
    }

    @Transactional
    public void remover(Long id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada: id=" + id));

        if (!"CANCELADA".equals(os.getStatus())) {
            throw new BusinessException(
                    "Só é possível remover OS com status CANCELADA. Status atual: " + os.getStatus());
        }

        repository.deleteById(id);
    }

    // ---------- Helpers ----------

    private String gerarNumeroOs() {
        int ano = Year.now().getValue();
        Integer proximo = repository.proximoNumeroSequencial(ano);
        int sequencial = (proximo == null ? 0 : proximo) + 1;
        return String.format("OS-%d-%04d", ano, sequencial);
    }

    private OrdemServicoResponse toResponse(OrdemServico os) {
        OrdemServicoResponse r = new OrdemServicoResponse();
        r.setId(os.getId());
        r.setNumeroOs(os.getNumeroOs());
        r.setVeiculoId(os.getVeiculo().getId());
        r.setVeiculoPlaca(os.getVeiculo().getPlaca());
        r.setVeiculoModelo(os.getVeiculo().getModelo());
        if (os.getTipoManutencao() != null) {
            r.setTipoManutencaoId(os.getTipoManutencao().getId());
            r.setTipoManutencaoNome(os.getTipoManutencao().getNome());
        }
        r.setDataAbertura(os.getDataAbertura());
        r.setDataConclusao(os.getDataConclusao());
        r.setKmAtual(os.getKmAtual());
        r.setTipoOficina(os.getTipoOficina());
        r.setOficinaNome(os.getOficinaNome());
        r.setValorPecas(os.getValorPecas());
        r.setValorMaoObra(os.getValorMaoObra());
        r.setValorTotal(os.getValorTotal());
        r.setPrevisaoConclusao(os.getPrevisaoConclusao());
        r.setStatus(os.getStatus());
        r.setObservacoes(os.getObservacoes());
        r.setCreatedAt(os.getCreatedAt());
        r.setUpdatedAt(os.getUpdatedAt());

        r.setItens(os.getItens().stream().map(item -> {
            OrdemServicoResponse.ItemResponse ir = new OrdemServicoResponse.ItemResponse();
            ir.setId(item.getId());
            ir.setDescricao(item.getDescricao());
            ir.setTipoItem(item.getTipoItem());
            ir.setQuantidade(item.getQuantidade());
            ir.setValorUnitario(item.getValorUnitario());
            ir.setValorTotal(item.getValorTotal());
            return ir;
        }).toList());

        return r;
    }
}