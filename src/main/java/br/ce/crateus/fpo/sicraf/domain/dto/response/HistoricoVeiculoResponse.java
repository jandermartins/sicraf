package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class HistoricoVeiculoResponse {

    // ============================================
    // Dados do veículo
    // ============================================
    private Long veiculoId;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
    private BigDecimal kmAtual;
    private String status;

    // ============================================
    // Resumo estatístico
    // ============================================
    private Long totalEventos;
    private Long totalChecklists;
    private Long totalAbastecimentos;
    private Long totalOcorrencias;
    private Long totalOrdensServico;

    private BigDecimal totalLitros;
    private BigDecimal totalGastoCombustivel;
    private BigDecimal totalGastoManutencao;
    private BigDecimal totalGastoOcorrencias;
    private BigDecimal totalGastoGeral;

    private BigDecimal consumoMedioKmLitro;

    // ============================================
    // Timeline
    // ============================================
    private List<HistoricoEventoResponse> eventos;
}