package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VeiculoResponse {
    private Long id;
    private String placa;
    private String renavam;
    private String chassi;
    private String marca;
    private String modelo;
    private Integer ano;
    private String cor;
    private String tipoCombustivel;
    private BigDecimal kmAtual;
    private BigDecimal capacidadeCarga;
    private String status;
    private String observacoes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
