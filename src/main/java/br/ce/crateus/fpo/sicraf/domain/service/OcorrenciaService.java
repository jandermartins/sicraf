package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.OcorrenciaRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.OcorrenciaResponse;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.Motorista;
import br.ce.crateus.fpo.sicraf.domain.model.Ocorrencia;
import br.ce.crateus.fpo.sicraf.domain.model.Veiculo;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.MotoristaRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.OcorrenciaRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OcorrenciaService {

    private static final Set<String> TIPOS_VALIDOS =
            Set.of("MULTA", "ACIDENTE", "AVARIA", "OUTRO");

    private static final Set<String> STATUS_VALIDOS =
            Set.of("PENDENTE", "PAGA", "RECORRIDA", "CANCELADA");

    private final OcorrenciaRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final MotoristaRepository motoristaRepository;

    public Page<OcorrenciaResponse> listar(String tipo, String status,
                                           Long veiculoId, Long motoristaId,
                                           LocalDate dataInicio, LocalDate dataFim,
                                           Pageable pageable) {
        return repository.findByFilters(tipo, status, veiculoId, motoristaId,
                dataInicio, dataFim, pageable).map(this::toResponse);
    }

    public OcorrenciaResponse buscarPorId(Long id) {
        Ocorrencia o = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocorrência não encontrada: id=" + id));
        return toResponse(o);
    }

    public List<OcorrenciaResponse> listarPorVeiculo(Long veiculoId) {
        return repository.findByVeiculoIdOrderByDataDesc(veiculoId).stream()
                .map(this::toResponse).toList();
    }

    public List<OcorrenciaResponse> listarPorMotorista(Long motoristaId) {
        return repository.findByMotoristaIdOrderByDataDesc(motoristaId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public OcorrenciaResponse criar(OcorrenciaRequest request) {
        Veiculo veiculo = veiculoRepository.findById(request.getVeiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Veículo não encontrado: id=" + request.getVeiculoId()));

        Motorista motorista = null;
        if (request.getMotoristaId() != null) {
            motorista = motoristaRepository.findById(request.getMotoristaId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Motorista não encontrado: id=" + request.getMotoristaId()));
        }

        // RN01: Tipo válido
        String tipoNormalizado = request.getTipo().toUpperCase().trim();
        if (!TIPOS_VALIDOS.contains(tipoNormalizado)) {
            throw new BusinessException("Tipo inválido: " + request.getTipo()
                    + ". Use MULTA, ACIDENTE, AVARIA ou OUTRO");
        }

        // RN02: Multa exige valor e prazo de recurso
        if ("MULTA".equals(tipoNormalizado)) {
            if (request.getValor() == null) {
                throw new BusinessException("Multa exige valor informado");
            }
            if (request.getPrazoRecurso() == null) {
                throw new BusinessException("Multa exige prazo para recurso");
            }
        }

        // RN03: Status inicial
        String statusNormalizado = request.getStatus() != null
                ? request.getStatus().toUpperCase()
                : "PENDENTE";
        if (!STATUS_VALIDOS.contains(statusNormalizado)) {
            throw new BusinessException("Status inválido: " + request.getStatus());
        }

        Ocorrencia ocorrencia = Ocorrencia.builder()
                .veiculo(veiculo)
                .motorista(motorista)
                .data(request.getData() != null ? request.getData() : LocalDate.now())
                .tipo(tipoNormalizado)
                .descricao(request.getDescricao())
                .valor(request.getValor())
                .prazoRecurso(request.getPrazoRecurso())
                .status(statusNormalizado)
                .anexoUrl(request.getAnexoUrl())
                .build();

        return toResponse(repository.save(ocorrencia));
    }

    @Transactional
    public OcorrenciaResponse atualizar(Long id, OcorrenciaRequest request) {
        Ocorrencia ocorrencia = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocorrência não encontrada: id=" + id));

        Veiculo veiculo = veiculoRepository.findById(request.getVeiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Veículo não encontrado: id=" + request.getVeiculoId()));

        Motorista motorista = null;
        if (request.getMotoristaId() != null) {
            motorista = motoristaRepository.findById(request.getMotoristaId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Motorista não encontrado: id=" + request.getMotoristaId()));
        }

        String tipoNormalizado = request.getTipo().toUpperCase().trim();
        if (!TIPOS_VALIDOS.contains(tipoNormalizado)) {
            throw new BusinessException("Tipo inválido: " + request.getTipo());
        }

        ocorrencia.setVeiculo(veiculo);
        ocorrencia.setMotorista(motorista);
        ocorrencia.setData(request.getData() != null ? request.getData() : ocorrencia.getData());
        ocorrencia.setTipo(tipoNormalizado);
        ocorrencia.setDescricao(request.getDescricao());
        ocorrencia.setValor(request.getValor());
        ocorrencia.setPrazoRecurso(request.getPrazoRecurso());
        ocorrencia.setAnexoUrl(request.getAnexoUrl());

        if (request.getStatus() != null) {
            String statusNormalizado = request.getStatus().toUpperCase();
            if (!STATUS_VALIDOS.contains(statusNormalizado)) {
                throw new BusinessException("Status inválido: " + request.getStatus());
            }
            ocorrencia.setStatus(statusNormalizado);
        }

        return toResponse(repository.save(ocorrencia));
    }

    // ============================================
    // Transições de status (workflow)
    // ============================================

    @Transactional
    public OcorrenciaResponse marcarComoPaga(Long id) {
        return mudarStatus(id, "PAGA");
    }

    @Transactional
    public OcorrenciaResponse marcarComoRecorrida(Long id) {
        return mudarStatus(id, "RECORRIDA");
    }

    @Transactional
    public OcorrenciaResponse cancelar(Long id) {
        return mudarStatus(id, "CANCELADA");
    }

    private OcorrenciaResponse mudarStatus(Long id, String novoStatus) {
        Ocorrencia ocorrencia = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocorrência não encontrada: id=" + id));

        if ("CANCELADA".equalsIgnoreCase(ocorrencia.getStatus())) {
            throw new BusinessException("Ocorrência já está cancelada e não pode ser alterada");
        }

        ocorrencia.setStatus(novoStatus);
        return toResponse(repository.save(ocorrencia));
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Ocorrência não encontrada: id=" + id);
        }
        repository.deleteById(id);
    }

    // ---------- Mapeamento ----------

    private OcorrenciaResponse toResponse(Ocorrencia o) {
        OcorrenciaResponse r = new OcorrenciaResponse();
        r.setId(o.getId());
        r.setVeiculoId(o.getVeiculo().getId());
        r.setVeiculoPlaca(o.getVeiculo().getPlaca());
        r.setVeiculoModelo(o.getVeiculo().getModelo());
        if (o.getMotorista() != null) {
            r.setMotoristaId(o.getMotorista().getId());
            r.setMotoristaNome(o.getMotorista().getNome());
        }
        r.setData(o.getData());
        r.setTipo(o.getTipo());
        r.setDescricao(o.getDescricao());
        r.setValor(o.getValor());
        r.setPrazoRecurso(o.getPrazoRecurso());
        r.setStatus(o.getStatus());
        r.setAnexoUrl(o.getAnexoUrl());
        r.setPrazoRecursoVencido(o.isPrazoRecursoVencido());
        r.setCreatedAt(o.getCreatedAt());
        r.setUpdatedAt(o.getUpdatedAt());
        return r;
    }
}