package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AbastecimentoResponse {
    private Long id;
    private Long veiculoId;
    private String veiculoPlaca;
    private String veiculoModelo;
    private Long motoristaId;
    private String motoristaNome;
    private LocalDate data;
    private BigDecimal kmAtual;
    private BigDecimal litros;
    private BigDecimal valorTotal;
    private BigDecimal precoPorLitro;
    private BigDecimal consumoKmLitro;
    private String tipoCombustivel;
    private String posto;
    private String comprovanteUrl;
    private String observacoes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}