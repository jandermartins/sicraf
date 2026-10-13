package br.ce.crateus.fpo.sicraf.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AbastecimentoRequest {

    @NotNull(message = "Veículo é obrigatório")
    private Long veiculoId;

    private Long motoristaId;

    @PastOrPresent(message = "A data não pode ser futura")
    private LocalDate data;

    @NotNull(message = "KM atual é obrigatório")
    @PositiveOrZero(message = "KM não pode ser negativo")
    private BigDecimal kmAtual;

    @NotNull(message = "Litros é obrigatório")
    @Positive(message = "Litros deve ser maior que zero")
    private BigDecimal litros;

    @NotNull(message = "Valor total é obrigatório")
    @Positive(message = "Valor deve ser maior que zero")
    private BigDecimal valorTotal;

    private String tipoCombustivel;
    private String posto;
    private String comprovanteUrl;
    private String observacoes;
}