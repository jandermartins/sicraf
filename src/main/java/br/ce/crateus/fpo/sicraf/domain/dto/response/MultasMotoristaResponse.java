package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MultasMotoristaResponse {

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String statusFiltro;

    // ============================================
    // Totais gerais
    // ============================================
    private Long totalMultas;
    private BigDecimal valorTotalMultas;
    private BigDecimal valorMedioPorMotorista;
    private Long motoristasComMultas;

    // ============================================
    // Ranking
    // ============================================
    private List<MultaMotoristaDTO> ranking;
    private List<MultaMotoristaDTO> top5;

    @Data
    public static class MultaMotoristaDTO {
        private Long motoristaId;
        private String nome;
        private String cpf;
        private String secretaria;
        private String status;

        private Long totalMultas;
        private BigDecimal valorTotal;
        private BigDecimal valorMedio;

        // Detalhamento por status
        private Long multasPendentes;
        private Long multasPagas;
        private Long multasRecorridas;

        private BigDecimal valorPendente;
        private BigDecimal valorPago;
        private BigDecimal valorRecorrido;

        // Última multa
        private LocalDate ultimaMulta;
        private String ultimaMultaDescricao;
    }
}