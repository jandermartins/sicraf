package br.ce.crateus.fpo.sicraf.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TipoManutencaoRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100)
    private String nome;

    @Size(max = 500)
    private String descricao;

    @NotBlank(message = "Tipo é obrigatório (PREVENTIVA ou CORRETIVA)")
    @Pattern(regexp = "PREVENTIVA|CORRETIVA", message = "Tipo deve ser PREVENTIVA ou CORRETIVA")
    private String tipo;

    @Pattern(regexp = "KM|MESES", message = "Unidade deve ser KM ou MESES")
    private String unidadePeriodicidade;

    @Positive(message = "Valor da periodicidade deve ser positivo")
    private Integer valorPeriodicidade;

    @PositiveOrZero(message = "Valor estimado não pode ser negativo")
    private BigDecimal valorEstimado;

    private String status;
    private String observacoes;
}
