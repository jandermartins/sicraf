package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChecklistResponse {
    private Long id;
    private Long veiculoId;
    private String veiculoPlaca;
    private String veiculoModelo;
    private Long motoristaId;
    private String motoristaNome;
    private LocalDateTime dataSaida;
    private BigDecimal kmSaida;
    private String observacoes;
    private String status;
    private List<ItemResponse> itens;
    private boolean todosVerificados;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class ItemResponse {
        private Long id;
        private String item;
        private boolean verificado;
        private String observacao;
    }
}
