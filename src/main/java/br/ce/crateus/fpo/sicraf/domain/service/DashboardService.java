package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.response.DashboardResponse;
import br.ce.crateus.fpo.sicraf.domain.model.*;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VeiculoRepository veiculoRepository;
    private final MotoristaRepository motoristaRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final AlertaManutencaoRepository alertaRepository;

    /**
     * Gera todos os KPIs do dashboard, com filtro opcional de período.
     * Se dataInicio/dataFim forem nulos, usa o mês atual.
     */
    public DashboardResponse obterDashboard(LocalDate dataInicio, LocalDate dataFim) {
        final LocalDate inicio = (dataInicio != null)
                ? dataInicio
                : LocalDate.now().withDayOfMonth(1);

        final LocalDate fim = (dataFim != null)
                ? dataFim
                : LocalDate.now();

        DashboardResponse response = new DashboardResponse();
        // Período padrão: mês atual
        if (dataInicio == null) {
            dataInicio = LocalDate.now().withDayOfMonth(1);
        }
        if (dataFim == null) {
            dataFim = LocalDate.now();
        }

        // ============================================
        // 1. KPIs de frota
        // ============================================
        List<Veiculo> veiculos = veiculoRepository.findAll();
        response.setTotalVeiculos((long) veiculos.size());
        response.setVeiculosAtivos(veiculos.stream()
                .filter(v -> "ATIVO".equalsIgnoreCase(v.getStatus())).count());
        response.setVeiculosEmManutencao(veiculos.stream()
                .filter(v -> "MANUTENCAO".equalsIgnoreCase(v.getStatus())).count());
        response.setVeiculosInativos(veiculos.stream()
                .filter(v -> "INATIVO".equalsIgnoreCase(v.getStatus())).count());

        // ============================================
        // 2. KPIs de motoristas
        // ============================================
        List<Motorista> motoristas = motoristaRepository.findAll();
        response.setTotalMotoristas((long) motoristas.size());
        response.setMotoristasAtivos(motoristas.stream()
                .filter(m -> "ATIVO".equalsIgnoreCase(m.getStatus())).count());

        LocalDate hoje = LocalDate.now();
        LocalDate limite30 = hoje.plusDays(30);

        response.setMotoristasCnhVencida(motoristas.stream()
                .filter(m -> m.getValidadeCnh() != null && m.getValidadeCnh().isBefore(hoje))
                .count());

        response.setMotoristasCnhVencendo30Dias(motoristas.stream()
                .filter(m -> m.getValidadeCnh() != null
                        && !m.getValidadeCnh().isBefore(hoje)
                        && !m.getValidadeCnh().isAfter(limite30))
                .count());

        // ============================================
        // 3. KPIs de alertas e OS
        // ============================================
        response.setAlertasManutencaoPendentes(
                (long) alertaRepository.findPendentes().size());

        response.setOrdensServicoAbertas(
                ordemServicoRepository.findByFilters(null, null, null,
                                org.springframework.data.domain.Pageable.unpaged())
                        .stream()
                        .filter(os -> os.getStatus().equals("ABERTA")
                                || os.getStatus().equals("EM_ANDAMENTO")
                                || os.getStatus().equals("AGUARDANDO_PECAS"))
                        .count());

        // ============================================
        // 4. Financeiro do período
        // ============================================
        List<Abastecimento> abastecimentos = abastecimentoRepository.findByFilters(
                null, null, dataInicio, dataFim,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        List<Ocorrencia> ocorrencias = ocorrenciaRepository.findByFilters(
                null, null, null, null, dataInicio, dataFim,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        List<OrdemServico> ordensServico = ordemServicoRepository.findAll().stream()
                .filter(os -> os.getDataAbertura() != null
                        && !os.getDataAbertura().toLocalDate().isBefore(inicio)
                        && !os.getDataAbertura().toLocalDate().isAfter(fim))
                .toList();

        BigDecimal custoCombustivel = abastecimentos.stream()
                .map(Abastecimento::getValorTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal custoManutencao = ordensServico.stream()
                .filter(os -> "CONCLUIDA".equalsIgnoreCase(os.getStatus()))
                .map(OrdemServico::getValorTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal custoOcorrencias = ocorrencias.stream()
                .filter(o -> "PAGA".equalsIgnoreCase(o.getStatus()))
                .map(Ocorrencia::getValor)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setCustoCombustivel(custoCombustivel);
        response.setCustoManutencao(custoManutencao);
        response.setCustoOcorrencias(custoOcorrencias);
        response.setCustoTotal(custoCombustivel.add(custoManutencao).add(custoOcorrencias));

        // Consumo médio da frota
        BigDecimal totalLitros = abastecimentos.stream()
                .map(Abastecimento::getLitros)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (abastecimentos.size() >= 2 && totalLitros.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal kmMax = abastecimentos.stream()
                    .map(Abastecimento::getKmAtual)
                    .filter(java.util.Objects::nonNull)
                    .max(BigDecimal::compareTo).orElse(null);
            BigDecimal kmMin = abastecimentos.stream()
                    .map(Abastecimento::getKmAtual)
                    .filter(java.util.Objects::nonNull)
                    .min(BigDecimal::compareTo).orElse(null);

            if (kmMax != null && kmMin != null && kmMax.compareTo(kmMin) > 0) {
                BigDecimal consumo = kmMax.subtract(kmMin)
                        .divide(totalLitros, 2, RoundingMode.HALF_UP);
                response.setConsumoMedioFrota(consumo);
            }
        }

        // ============================================
        // 5. Top 5 veículos mais caros
        // ============================================
        response.setTop5VeiculosMaisCaros(calcularTop5Veiculos(
                veiculos, abastecimentos, ordensServico));

        // ============================================
        // 6. Top 5 motoristas com multas
        // ============================================
        response.setTop5MotoristasComMultas(
                calcularTop5MotoristasComMultas(motoristas, ocorrencias));

        // ============================================
        // 7. Alertas de CNH
        // ============================================
        response.setAlertasCnh(calcularAlertasCnh(motoristas));

        return response;
    }

    // ============================================
    // Helpers privados
    // ============================================

    private List<DashboardResponse.VeiculoCustoDTO> calcularTop5Veiculos(
            List<Veiculo> veiculos,
            List<Abastecimento> abastecimentos,
            List<OrdemServico> ordensServico) {

        List<DashboardResponse.VeiculoCustoDTO> lista = new ArrayList<>();

        for (Veiculo veiculo : veiculos) {
            BigDecimal comb = abastecimentos.stream()
                    .filter(a -> a.getVeiculo().getId().equals(veiculo.getId()))
                    .map(Abastecimento::getValorTotal)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal manut = ordensServico.stream()
                    .filter(os -> os.getVeiculo().getId().equals(veiculo.getId()))
                    .filter(os -> "CONCLUIDA".equalsIgnoreCase(os.getStatus()))
                    .map(OrdemServico::getValorTotal)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal total = comb.add(manut);

            if (total.compareTo(BigDecimal.ZERO) > 0) {
                DashboardResponse.VeiculoCustoDTO dto = new DashboardResponse.VeiculoCustoDTO();
                dto.setVeiculoId(veiculo.getId());
                dto.setPlaca(veiculo.getPlaca());
                dto.setModelo(veiculo.getMarca() + " " + veiculo.getModelo());
                dto.setCustoCombustivel(comb);
                dto.setCustoManutencao(manut);
                dto.setCustoTotal(total);
                lista.add(dto);
            }
        }

        return lista.stream()
                .sorted(Comparator.comparing(DashboardResponse.VeiculoCustoDTO::getCustoTotal).reversed())
                .limit(5)
                .toList();
    }

    private List<DashboardResponse.MotoristaMultaDTO> calcularTop5MotoristasComMultas(
            List<Motorista> motoristas,
            List<Ocorrencia> ocorrencias) {

        List<DashboardResponse.MotoristaMultaDTO> lista = new ArrayList<>();

        for (Motorista motorista : motoristas) {
            List<Ocorrencia> multas = ocorrencias.stream()
                    .filter(o -> o.getMotorista() != null
                            && o.getMotorista().getId().equals(motorista.getId()))
                    .filter(o -> "MULTA".equalsIgnoreCase(o.getTipo()))
                    .toList();

            if (!multas.isEmpty()) {
                BigDecimal valor = multas.stream()
                        .map(Ocorrencia::getValor)
                        .filter(java.util.Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                DashboardResponse.MotoristaMultaDTO dto = new DashboardResponse.MotoristaMultaDTO();
                dto.setMotoristaId(motorista.getId());
                dto.setNome(motorista.getNome());
                dto.setTotalMultas((long) multas.size());
                dto.setValorTotal(valor);
                lista.add(dto);
            }
        }

        return lista.stream()
                .sorted(Comparator.comparing(DashboardResponse.MotoristaMultaDTO::getValorTotal).reversed())
                .limit(5)
                .toList();
    }

    private List<DashboardResponse.AlertaCnhDTO> calcularAlertasCnh(List<Motorista> motoristas) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite30 = hoje.plusDays(30);

        List<DashboardResponse.AlertaCnhDTO> lista = new ArrayList<>();

        for (Motorista m : motoristas) {
            if (m.getValidadeCnh() == null) continue;

            boolean vencida = m.getValidadeCnh().isBefore(hoje);
            boolean vencendo = !vencida && !m.getValidadeCnh().isAfter(limite30);

            if (vencida || vencendo) {
                DashboardResponse.AlertaCnhDTO dto = new DashboardResponse.AlertaCnhDTO();
                dto.setMotoristaId(m.getId());
                dto.setNome(m.getNome());
                dto.setCnh(m.getCnh());
                dto.setValidadeCnh(m.getValidadeCnh());
                dto.setDiasRestantes(ChronoUnit.DAYS.between(hoje, m.getValidadeCnh()));
                dto.setStatus(vencida ? "VENCIDA" : "VENCENDO");
                lista.add(dto);
            }
        }

        return lista.stream()
                .sorted(Comparator.comparing(DashboardResponse.AlertaCnhDTO::getValidadeCnh))
                .toList();
    }
}