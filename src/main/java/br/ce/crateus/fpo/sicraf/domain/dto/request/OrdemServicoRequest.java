package br.ce.crateus.fpo.sicraf.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrdemServicoRequest {

    @NotNull(message = "Veículo é obrigatório")
    private Long veiculoId;

    private Long tipoManutencaoId;

    @PositiveOrZero(message = "KM não pode ser negativo")
    private BigDecimal kmAtual;

    @NotBlank(message = "Tipo de oficina é obrigatório (INTERNA ou EXTERNA)")
    private String tipoOficina;

    private String oficinaNome;

    private LocalDate previsaoConclusao;

    private String observacoes;

    private List<ItemRequest> itens;

    @Data
    public static class ItemRequest {
        @NotBlank(message = "Descrição do item é obrigatória")
        private String descricao;

        @NotBlank(message = "Tipo do item é obrigatório (PECA, MAO_DE_OBRA ou SERVICO)")
        private String tipoItem;

        @PositiveOrZero(message = "Quantidade não pode ser negativa")
        private BigDecimal quantidade;

        @PositiveOrZero(message = "Valor unitário não pode ser negativo")
        private BigDecimal valorUnitario;
    }
}