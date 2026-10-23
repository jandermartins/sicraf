package br.ce.crateus.fpo.sicraf.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AtualizarItensRequest {

    private List<ItemUpdate> itens;

    @Data
    public static class ItemUpdate {
        @NotBlank(message = "Nome do item é obrigatório")
        private String item;

        private boolean verificado;

        private String observacao;
    }
}