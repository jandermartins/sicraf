package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.response.AlertaManutencaoResponse;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.*;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertaManutencaoService {

    private final AlertaManutencaoRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final TipoManutencaoRepository tipoManutencaoRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    public Page<AlertaManutencaoResponse> listar(String status, Long veiculoId,
                                                 Long tipoManutencaoId, Pageable pageable) {
        return repository.findByFilters(status, veiculoId, tipoManutencaoId, pageable)
                .map(this::toResponse);
    }

    public AlertaManutencaoResponse buscarPorId(Long id) {
        AlertaManutencao a = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado: id=" + id));
        return toResponse(a);
    }

    public List<AlertaManutencaoResponse> listarPendentes() {
        return repository.findPendentes().stream()
                .map(this::toResponse).toList();
    }

    /**
     * Verifica todos os veículos e tipos de manutenção preventiva,
     * gerando alertas quando necessário. Executado pelo scheduler.
     */
    @Transactional
    public int verificarEGerarAlertas() {
        List<Veiculo> veiculos = veiculoRepository.findAll().stream()
                .filter(v -> "ATIVO".equalsIgnoreCase(v.getStatus()))
                .toList();

        List<TipoManutencao> tiposPreventivos = tipoManutencaoRepository.findAtivosPreventivos();

        int alertasGerados = 0;

        for (Veiculo veiculo : veiculos) {
            for (TipoManutencao tipo : tiposPreventivos) {
                if (deveGerarAlerta(veiculo, tipo)) {
                    if (repository.findAtivoPorVeiculoETipo(veiculo.getId(), tipo.getId()).isEmpty()) {
                        gerarAlerta(veiculo, tipo);
                        alertasGerados++;
                    }
                }
            }
        }

        if (alertasGerados > 0) {
            log.info("SICRAF: {} alertas de manutenção gerados.", alertasGerados);
        }

        return alertasGerados;
    }

    /**
     * Marca um alerta como CONCLUIDO (chamado quando uma OS é criada).
     */
    @Transactional
    public void concluirPorVeiculoETipo(Long veiculoId, Long tipoManutencaoId) {
        repository.findAtivoPorVeiculoETipo(veiculoId, tipoManutencaoId).ifPresent(alerta -> {
            alerta.setStatus("CONCLUIDO");
            repository.save(alerta);
        });
    }

    @Transactional
    public AlertaManutencaoResponse cancelar(Long id) {
        AlertaManutencao alerta = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado: id=" + id));

        if ("CONCLUIDO".equalsIgnoreCase(alerta.getStatus())) {
            throw new BusinessException("Alerta já está concluído e não pode ser cancelado");
        }

        alerta.setStatus("CANCELADO");
        return toResponse(repository.save(alerta));
    }

    @Transactional
    public AlertaManutencaoResponse agendar(Long id) {
        AlertaManutencao alerta = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado: id=" + id));

        if (!"PENDENTE".equalsIgnoreCase(alerta.getStatus())) {
            throw new BusinessException("Só é possível agendar alertas com status PENDENTE");
        }

        alerta.setStatus("AGENDADO");
        return toResponse(repository.save(alerta));
    }

    // ============================================
    // Regras de negócio (privadas)
    // ============================================

    private boolean deveGerarAlerta(Veiculo veiculo, TipoManutencao tipo) {
        if (veiculo.getKmAtual() == null) return false;

        // Busca a última OS CONCLUIDA para este veículo + tipo
        BigDecimal kmUltimaManutencao = obterKmUltimaManutencao(veiculo.getId(), tipo.getId());

        if ("KM".equalsIgnoreCase(String.valueOf(tipo.getUnidadePeriodicidade()))) {
            if (tipo.getValorPeriodicidade() == null || tipo.getValorPeriodicidade() == 0) {
                return false;
            }

            BigDecimal intervalo = BigDecimal.valueOf(tipo.getValorPeriodicidade());
            BigDecimal kmProxima = kmUltimaManutencao.add(intervalo);
            BigDecimal limite90 = kmProxima.subtract(intervalo.multiply(new BigDecimal("0.10")));

            return veiculo.getKmAtual().compareTo(limite90) >= 0;
        }

        if ("MESES".equalsIgnoreCase(String.valueOf(tipo.getUnidadePeriodicidade()))) {
            // Se nunca houve manutenção, gera alerta
            return true;
        }

        return false;
    }

    private BigDecimal obterKmUltimaManutencao(Long veiculoId, Long tipoManutencaoId) {
        return ordemServicoRepository
                .findTopByVeiculoIdAndTipoManutencaoIdAndStatusOrderByDataConclusaoDesc(
                        veiculoId, tipoManutencaoId, "CONCLUIDA")
                .map(OrdemServico::getKmAtual)
                .orElse(BigDecimal.ZERO);
    }

    private void gerarAlerta(Veiculo veiculo, TipoManutencao tipo) {
        AlertaManutencao alerta = AlertaManutencao.builder()
                .veiculo(veiculo)
                .tipoManutencao(tipo)
                .kmUltimaManutencao(veiculo.getKmAtual())
                .dataUltimaManutencao(LocalDate.now())
                .kmProximaManutencao(calcularProximaKm(veiculo, tipo))
                .dataProximaManutencao(calcularProximaData(tipo))
                .status("PENDENTE")
                .notificado(false)
                .build();

        repository.save(alerta);
    }

    private BigDecimal calcularProximaKm(Veiculo veiculo, TipoManutencao tipo) {
        if (tipo.getValorPeriodicidade() == null) return null;
        return veiculo.getKmAtual().add(BigDecimal.valueOf(tipo.getValorPeriodicidade()));
    }

    private LocalDate calcularProximaData(TipoManutencao tipo) {
        if (tipo.getValorPeriodicidade() == null) return null;
        if ("MESES".equalsIgnoreCase(String.valueOf(tipo.getUnidadePeriodicidade()))) {
            return LocalDate.now().plusMonths(tipo.getValorPeriodicidade());
        }
        return null;
    }

    // ---------- Mapeamento ----------

    private AlertaManutencaoResponse toResponse(AlertaManutencao a) {
        AlertaManutencaoResponse r = new AlertaManutencaoResponse();
        r.setId(a.getId());
        r.setVeiculoId(a.getVeiculo().getId());
        r.setVeiculoPlaca(a.getVeiculo().getPlaca());
        r.setVeiculoModelo(a.getVeiculo().getModelo());
        r.setTipoManutencaoId(a.getTipoManutencao().getId());
        r.setTipoManutencaoNome(a.getTipoManutencao().getNome());
        if (a.getTipoManutencao().getUnidadePeriodicidade() != null) {
            r.setTipoManutencaoUnidade(a.getTipoManutencao().getUnidadePeriodicidade().name());
        }
        r.setKmUltimaManutencao(a.getKmUltimaManutencao());
        r.setDataUltimaManutencao(a.getDataUltimaManutencao());
        r.setKmProximaManutencao(a.getKmProximaManutencao());
        r.setDataProximaManutencao(a.getDataProximaManutencao());
        r.setKmAtualVeiculo(a.getVeiculo().getKmAtual());
        r.setKmRestantes(a.getKmRestantes());
        r.setDiasRestantes(a.getDiasRestantes());
        r.setStatus(a.getStatus());
        r.setNotificado(a.isNotificado());
        r.setCreatedAt(a.getCreatedAt());
        r.setUpdatedAt(a.getUpdatedAt());
        return r;
    }
}