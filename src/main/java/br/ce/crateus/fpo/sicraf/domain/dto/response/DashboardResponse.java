package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class DashboardResponse {

    // ============================================
    // KPIs de frota
    // ============================================
    private Long totalVeiculos;
    private Long veiculosAtivos;
    private Long veiculosEmManutencao;
    private Long veiculosInativos;

    // ============================================
    // KPIs de motoristas
    // ============================================
    private Long totalMotoristas;
    private Long motoristasAtivos;
    private Long motoristasCnhVencendo30Dias;
    private Long motoristasCnhVencida;

    // ============================================
    // KPIs de alertas
    // ============================================
    private Long alertasManutencaoPendentes;
    private Long ordensServicoAbertas;

    // ============================================
    // Financeiro (período)
    // ============================================
    private BigDecimal custoCombustivel;
    private BigDecimal custoManutencao;
    private BigDecimal custoOcorrencias;
    private BigDecimal custoTotal;
    private BigDecimal consumoMedioFrota; // KM/L

    // ============================================
    // Listas para gráficos
    // ============================================
    private List<VeiculoCustoDTO> top5VeiculosMaisCaros;
    private List<MotoristaMultaDTO> top5MotoristasComMultas;
    private List<AlertaCnhDTO> alertasCnh;

    @Data
    public static class VeiculoCustoDTO {
        private Long veiculoId;
        private String placa;
        private String modelo;
        private BigDecimal custoTotal;
        private BigDecimal custoCombustivel;
        private BigDecimal custoManutencao;
    }

    @Data
    public static class MotoristaMultaDTO {
        private Long motoristaId;
        private String nome;
        private Long totalMultas;
        private BigDecimal valorTotal;
    }

    @Data
    public static class AlertaCnhDTO {
        private Long motoristaId;
        private String nome;
        private String cnh;
        private LocalDate validadeCnh;
        private Long diasRestantes;
        private String status; // VENCIDA ou VENCENDO
    }
}