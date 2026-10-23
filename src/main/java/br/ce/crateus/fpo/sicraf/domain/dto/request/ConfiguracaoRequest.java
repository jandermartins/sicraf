package br.ce.crateus.fpo.sicraf.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfiguracaoRequest {

    @NotBlank(message = "Chave é obrigatória")
    private String chave;

    private String valor;
    private String descricao;
    private String tipo;
}