package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OcorrenciaResponse {
    private Long id;
    private Long veiculoId;
    private String veiculoPlaca;
    private String veiculoModelo;
    private Long motoristaId;
    private String motoristaNome;
    private LocalDate data;
    private String tipo;
    private String descricao;
    private BigDecimal valor;
    private LocalDate prazoRecurso;
    private String status;
    private String anexoUrl;
    private boolean prazoRecursoVencido;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}