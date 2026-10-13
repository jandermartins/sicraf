package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AlertaManutencaoResponse {
    private Long id;
    private Long veiculoId;
    private String veiculoPlaca;
    private String veiculoModelo;
    private Long tipoManutencaoId;
    private String tipoManutencaoNome;
    private String tipoManutencaoUnidade; // KM ou MESES
    private BigDecimal kmUltimaManutencao;
    private LocalDate dataUltimaManutencao;
    private BigDecimal kmProximaManutencao;
    private LocalDate dataProximaManutencao;
    private BigDecimal kmAtualVeiculo;
    private BigDecimal kmRestantes;
    private Long diasRestantes;
    private String status;
    private boolean notificado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}