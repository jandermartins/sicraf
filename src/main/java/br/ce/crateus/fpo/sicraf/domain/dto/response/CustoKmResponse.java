package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CustoKmResponse {

    private LocalDate dataInicio;
    private LocalDate dataFim;

    // ============================================
    // Totais da frota
    // ============================================
    private BigDecimal custoTotalFrota;
    private BigDecimal custoMedioPorKmFrota;
    private BigDecimal kmTotalFrota;

    // ============================================
    // Detalhamento por veículo
    // ============================================
    private List<CustoKmVeiculoDTO> veiculos;

    @Data
    public static class CustoKmVeiculoDTO {
        private Long veiculoId;
        private String placa;
        private String marca;
        private String modelo;
        private Integer ano;
        private String status;

        // Custos
        private BigDecimal custoCombustivel;
        private BigDecimal custoManutencao;
        private BigDecimal custoOcorrencias;
        private BigDecimal custoTotal;

        // KM e consumo
        private BigDecimal kmInicial;
        private BigDecimal kmFinal;
        private BigDecimal kmRodado;
        private BigDecimal litrosConsumidos;
        private BigDecimal consumoKmLitro;

        // Custo por KM
        private BigDecimal custoPorKm;
        private BigDecimal custoPorKmCombustivel;
        private BigDecimal custoPorKmManutencao;
    }
}