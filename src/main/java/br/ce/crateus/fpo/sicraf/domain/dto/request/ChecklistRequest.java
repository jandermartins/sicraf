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
     * Lista de itens verificados. Se não enviada, todos os itens padrão
     * são criados como NÃO verificados.
     */
    private List<ItemRequest> itens;

    @Data
    public static class ItemRequest {
        private String item;
        private boolean verificado;
        private String observacao;
    }
}
