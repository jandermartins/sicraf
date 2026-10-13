package br.ce.crateus.fpo.sicraf.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OcorrenciaRequest {

    @NotNull(message = "Veículo é obrigatório")
    private Long veiculoId;

    private Long motoristaId;

    @PastOrPresent(message = "A data não pode ser futura")
    private LocalDate data;

    @NotBlank(message = "Tipo é obrigatório")
    private String tipo;  // MULTA, ACIDENTE, AVARIA, OUTRO

    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 2000)
    private String descricao;

    @PositiveOrZero(message = "Valor não pode ser negativo")
    private BigDecimal valor;

    private LocalDate prazoRecurso;

    private String status;  // PENDENTE, PAGA, RECORRIDA, CANCELADA

    private String anexoUrl;
}