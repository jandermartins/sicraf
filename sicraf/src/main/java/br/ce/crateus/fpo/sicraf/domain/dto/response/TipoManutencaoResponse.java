package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TipoManutencaoResponse {
    private Long id;
    private String nome;
    private String descricao;
    private String tipo;
    private String unidadePeriodicidade;
    private Integer valorPeriodicidade;
    private BigDecimal valorEstimado;
    private String status;
    private String observacoes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
