package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.response.MultasMotoristaResponse;
import br.ce.crateus.fpo.sicraf.domain.model.Motorista;
import br.ce.crateus.fpo.sicraf.domain.model.Ocorrencia;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.MotoristaRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.OcorrenciaRepository;
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
public class RelatorioMultasService {

    private final OcorrenciaRepository ocorrenciaRepository;
    private final MotoristaRepository motoristaRepository;

    /**
     * Gera o relatório de multas por motorista.
     *
     * @param dataInicio (opcional) início do período
     * @param dataFim (opcional) fim do período
     * @param statusFiltro (opcional) PENDENTE, PAGA, RECORRIDA
     */
    public MultasMotoristaResponse gerar(LocalDate dataInicio,
                                         LocalDate dataFim,
                                         String statusFiltro) {

        final LocalDate inicio = (dataInicio != null)
                ? dataInicio
                : LocalDate.now().withDayOfMonth(1);

        final LocalDate fim = (dataFim != null)
                ? dataFim
                : LocalDate.now();

        final String status = (statusFiltro != null && !statusFiltro.isBlank())
                ? statusFiltro.toUpperCase()
                : null;

        // 1. Buscar todas as ocorrências do tipo MULTA no período
        List<Ocorrencia> multas = ocorrenciaRepository.findByFilters(
                "MULTA", status, null, null, inicio, fim, Pageable.unpaged()).getContent();

        // 2. Buscar todos os motoristas
        List<Motorista> motoristas = motoristaRepository.findAll();

        // 3. Agrupar por motorista
        List<MultasMotoristaResponse.MultaMotoristaDTO> ranking = new ArrayList<>();

        for (Motorista motorista : motoristas) {
            List<Ocorrencia> multasMotorista = multas.stream()
                    .filter(m -> m.getMotorista() != null
                            && m.getMotorista().getId().equals(motorista.getId()))
                    .toList();

            if (!multasMotorista.isEmpty()) {
                ranking.add(construirDTO(motorista, multasMotorista));
            }
        }

        // 4. Ordenar por valor total (maior primeiro)
        ranking.sort(Comparator.comparing(
                MultasMotoristaResponse.MultaMotoristaDTO::getValorTotal).reversed());

        // 5. Calcular totais
        MultasMotoristaResponse response = new MultasMotoristaResponse();
        response.setDataInicio(inicio);
        response.setDataFim(fim);
        response.setStatusFiltro(status);

        BigDecimal valorTotal = ranking.stream()
                .map(MultasMotoristaResponse.MultaMotoristaDTO::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalMultas = ranking.stream()
                .mapToLong(MultasMotoristaResponse.MultaMotoristaDTO::getTotalMultas)
                .sum();

        response.setTotalMultas(totalMultas);
        response.setValorTotalMultas(valorTotal);
        response.setMotoristasComMultas((long) ranking.size());

        if (!ranking.isEmpty()) {
            response.setValorMedioPorMotorista(
                    valorTotal.divide(BigDecimal.valueOf(ranking.size()), 2, RoundingMode.HALF_UP));
        } else {
            response.setValorMedioPorMotorista(BigDecimal.ZERO);
        }

        response.setRanking(ranking);
        response.setTop5(ranking.stream().limit(5).toList());

        return response;
    }

    // ============================================
    // Construção do DTO por motorista
    // ============================================

    private MultasMotoristaResponse.MultaMotoristaDTO construirDTO(
            Motorista motorista, List<Ocorrencia> multas) {

        MultasMotoristaResponse.MultaMotoristaDTO dto = new MultasMotoristaResponse.MultaMotoristaDTO();
        dto.setMotoristaId(motorista.getId());
        dto.setNome(motorista.getNome());
        dto.setCpf(motorista.getCpf());
        dto.setSecretaria(motorista.getSecretaria());
        dto.setStatus(motorista.getStatus());

        // Total
        dto.setTotalMultas((long) multas.size());

        BigDecimal valorTotal = multas.stream()
                .map(Ocorrencia::getValor)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setValorTotal(valorTotal);

        if (!multas.isEmpty()) {
            dto.setValorMedio(valorTotal.divide(
                    BigDecimal.valueOf(multas.size()), 2, RoundingMode.HALF_UP));
        } else {
            dto.setValorMedio(BigDecimal.ZERO);
        }

        // Por status
        List<Ocorrencia> pendentes = multas.stream()
                .filter(o -> "PENDENTE".equalsIgnoreCase(o.getStatus())).toList();
        List<Ocorrencia> pagas = multas.stream()
                .filter(o -> "PAGA".equalsIgnoreCase(o.getStatus())).toList();
        List<Ocorrencia> recorridas = multas.stream()
                .filter(o -> "RECORRIDA".equalsIgnoreCase(o.getStatus())).toList();

        dto.setMultasPendentes((long) pendentes.size());
        dto.setMultasPagas((long) pagas.size());
        dto.setMultasRecorridas((long) recorridas.size());

        dto.setValorPendente(somar(pendentes));
        dto.setValorPago(somar(pagas));
        dto.setValorRecorrido(somar(recorridas));

        // Última multa
        multas.stream()
                .filter(o -> o.getData() != null)
                .max(Comparator.comparing(Ocorrencia::getData))
                .ifPresent(ultima -> {
                    dto.setUltimaMulta(ultima.getData());
                    dto.setUltimaMultaDescricao(ultima.getDescricao());
                });

        return dto;
    }

    private BigDecimal somar(List<Ocorrencia> ocorrencias) {
        return ocorrencias.stream()
                .map(Ocorrencia::getValor)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}