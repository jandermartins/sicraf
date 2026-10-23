package br.ce.crateus.fpo.sicraf.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ChecklistRequest {

    @NotNull(message = "Veículo é obrigatório")
    private Long veiculoId;

    @NotNull(message = "Motorista é obrigatório")
    private Long motoristaId;

    @PositiveOrZero(message = "KM não pode ser negativo")
    private BigDecimal kmSaida;

    private String observacoes;

    /**
     * Lista de itens do check-list. Se não enviada, o backend usa os itens padrão.
     */
    private List<ItemRequest> itens;

    @Data
    public static class ItemRequest {
        /** Nome do item (ex: "Nível de Óleo", "CRLV do veículo"). */
        private String item;

        /** Se o item foi verificado ou não. */
        private boolean verificado;

        /** Observação opcional sobre o item. */
        private String observacao;
    }
}