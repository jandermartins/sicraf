package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.response.CustoKmResponse;
import br.ce.crateus.fpo.sicraf.domain.model.Abastecimento;
import br.ce.crateus.fpo.sicraf.domain.model.Ocorrencia;
import br.ce.crateus.fpo.sicraf.domain.model.OrdemServico;
import br.ce.crateus.fpo.sicraf.domain.model.Veiculo;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioCustoKmService {

    private final VeiculoRepository veiculoRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    /**
     * Gera o relatório de custo por KM para todos os veículos ativos no período.
     *
     * @param dataInicio Data inicial do período (obrigatória)
     * @param dataFim Data final do período (obrigatória)
     * @param veiculoId (opcional) filtra por veículo específico
     */
    public CustoKmResponse gerar(LocalDate dataInicio, LocalDate dataFim, Long veiculoId) {

        // Período padrão: mês atual, se não informado
        final LocalDate inicio = (dataInicio != null)
                ? dataInicio
                : LocalDate.now().withDayOfMonth(1);

        final LocalDate fim = (dataFim != null)
                ? dataFim
                : LocalDate.now();

        // 1. Buscar veículos
        List<Veiculo> veiculos = (veiculoId != null)
                ? veiculoRepository.findById(veiculoId).map(List::of).orElse(List.of())
                : veiculoRepository.findAll();

        // 2. Buscar abastecimentos, ocorrências e OS do período
        List<Abastecimento> todosAbastecimentos = abastecimentoRepository.findByFilters(
                null, null, inicio, fim, Pageable.unpaged()).getContent();

        List<Ocorrencia> todasOcorrencias = ocorrenciaRepository.findByFilters(
                null, null, null, null, inicio, fim, Pageable.unpaged()).getContent();

        List<OrdemServico> todasOrdensServico = ordemServicoRepository.findAll().stream()
                .filter(os -> os.getDataAbertura() != null
                        && !os.getDataAbertura().toLocalDate().isBefore(inicio)
                        && !os.getDataAbertura().toLocalDate().isAfter(fim))
                .toList();

        // 3. Calcular custo por veículo
        List<CustoKmResponse.CustoKmVeiculoDTO> veiculosDTO = new ArrayList<>();
        BigDecimal custoTotalFrota = BigDecimal.ZERO;
        BigDecimal kmTotalFrota = BigDecimal.ZERO;

        for (Veiculo veiculo : veiculos) {
            CustoKmResponse.CustoKmVeiculoDTO dto = calcularCustoVeiculo(
                    veiculo, todosAbastecimentos, todasOcorrencias, todasOrdensServico);

            veiculosDTO.add(dto);
            custoTotalFrota = custoTotalFrota.add(dto.getCustoTotal());
            kmTotalFrota = kmTotalFrota.add(dto.getKmRodado());
        }

        // 4. Ordenar por custo por KM (mais caro primeiro)
        veiculosDTO.sort(Comparator.comparing(
                CustoKmResponse.CustoKmVeiculoDTO::getCustoPorKm,
                Comparator.nullsLast(Comparator.reverseOrder())));

        // 5. Montar resposta
        CustoKmResponse response = new CustoKmResponse();
        response.setDataInicio(inicio);
        response.setDataFim(fim);
        response.setVeiculos(veiculosDTO);
        response.setCustoTotalFrota(custoTotalFrota);
        response.setKmTotalFrota(kmTotalFrota);

        if (kmTotalFrota.compareTo(BigDecimal.ZERO) > 0) {
            response.setCustoMedioPorKmFrota(
                    custoTotalFrota.divide(kmTotalFrota, 4, RoundingMode.HALF_UP));
        } else {
            response.setCustoMedioPorKmFrota(BigDecimal.ZERO);
        }

        return response;
    }

    // ============================================
    // Cálculo por veículo
    // ============================================

    private CustoKmResponse.CustoKmVeiculoDTO calcularCustoVeiculo(
            Veiculo veiculo,
            List<Abastecimento> abastecimentos,
            List<Ocorrencia> ocorrencias,
            List<OrdemServico> ordensServico) {

        // Filtra por veículo
        List<Abastecimento> abastVeiculo = abastecimentos.stream()
                .filter(a -> a.getVeiculo().getId().equals(veiculo.getId()))
                .sorted(Comparator.comparing(Abastecimento::getKmAtual))
                .toList();

        List<OrdemServico> osVeiculo = ordensServico.stream()
                .filter(os -> os.getVeiculo().getId().equals(veiculo.getId()))
                .filter(os -> "CONCLUIDA".equalsIgnoreCase(os.getStatus()))
                .toList();

        List<Ocorrencia> ocorrenciasVeiculo = ocorrencias.stream()
                .filter(o -> o.getVeiculo().getId().equals(veiculo.getId()))
                .filter(o -> "PAGA".equalsIgnoreCase(o.getStatus()))
                .toList();

        // Custos
        BigDecimal custoCombustivel = abastVeiculo.stream()
                .map(Abastecimento::getValorTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal custoManutencao = osVeiculo.stream()
                .map(OrdemServico::getValorTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal custoOcorrencias = ocorrenciasVeiculo.stream()
                .map(Ocorrencia::getValor)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal custoTotal = custoCombustivel.add(custoManutencao).add(custoOcorrencias);

        // KM rodado = km do último abastecimento - km do primeiro
        BigDecimal kmInicial = null;
        BigDecimal kmFinal = null;
        BigDecimal kmRodado = BigDecimal.ZERO;
        BigDecimal litrosConsumidos = BigDecimal.ZERO;

        if (!abastVeiculo.isEmpty()) {
            kmInicial = abastVeiculo.get(0).getKmAtual();
            kmFinal = abastVeiculo.get(abastVeiculo.size() - 1).getKmAtual();

            litrosConsumidos = abastVeiculo.stream()
                    .map(Abastecimento::getLitros)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (kmInicial != null && kmFinal != null && kmFinal.compareTo(kmInicial) > 0) {
                kmRodado = kmFinal.subtract(kmInicial);
            }
        }

        // Consumo (KM/L)
        BigDecimal consumo = null;
        if (kmRodado.compareTo(BigDecimal.ZERO) > 0
                && litrosConsumidos.compareTo(BigDecimal.ZERO) > 0) {
            consumo = kmRodado.divide(litrosConsumidos, 2, RoundingMode.HALF_UP);
        }

        // Custo por KM (4 casas decimais para precisão)
        BigDecimal custoPorKm = null;
        BigDecimal custoPorKmComb = null;
        BigDecimal custoPorKmManut = null;

        if (kmRodado.compareTo(BigDecimal.ZERO) > 0) {
            custoPorKm = custoTotal.divide(kmRodado, 4, RoundingMode.HALF_UP);
            custoPorKmComb = custoCombustivel.divide(kmRodado, 4, RoundingMode.HALF_UP);
            custoPorKmManut = custoManutencao.divide(kmRodado, 4, RoundingMode.HALF_UP);
        }

        // Montar DTO
        CustoKmResponse.CustoKmVeiculoDTO dto = new CustoKmResponse.CustoKmVeiculoDTO();
        dto.setVeiculoId(veiculo.getId());
        dto.setPlaca(veiculo.getPlaca());
        dto.setMarca(veiculo.getMarca());
        dto.setModelo(veiculo.getModelo());
        dto.setAno(veiculo.getAno());
        dto.setStatus(veiculo.getStatus());

        dto.setCustoCombustivel(custoCombustivel);
        dto.setCustoManutencao(custoManutencao);
        dto.setCustoOcorrencias(custoOcorrencias);
        dto.setCustoTotal(custoTotal);

        dto.setKmInicial(kmInicial);
        dto.setKmFinal(kmFinal);
        dto.setKmRodado(kmRodado);
        dto.setLitrosConsumidos(litrosConsumidos);
        dto.setConsumoKmLitro(consumo);

        dto.setCustoPorKm(custoPorKm);
        dto.setCustoPorKmCombustivel(custoPorKmComb);
        dto.setCustoPorKmManutencao(custoPorKmManut);

        return dto;
    }
}