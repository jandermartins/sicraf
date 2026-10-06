package br.ce.crateus.fpo.sicraf.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VeiculoRequest {

    @NotBlank(message = "Placa é obrigatória")
    @Size(max = 8, message = "Placa deve ter no máximo 8 caracteres")
    private String placa;

    @Size(max = 11)
    private String renavam;

    @Size(max = 17)
    private String chassi;

    @NotBlank(message = "Marca é obrigatória")
    private String marca;

    @NotBlank(message = "Modelo é obrigatório")
    private String modelo;

    @Min(value = 1900, message = "Ano inválido")
    @Max(value = 2100, message = "Ano inválido")
    private Integer ano;

    private String cor;
    private String tipoCombustivel;

    @PositiveOrZero(message = "KM não pode ser negativo")
    private BigDecimal kmAtual;

    @PositiveOrZero(message = "Capacidade não pode ser negativa")
    private BigDecimal capacidadeCarga;

    private String status;
    private String observacoes;
}