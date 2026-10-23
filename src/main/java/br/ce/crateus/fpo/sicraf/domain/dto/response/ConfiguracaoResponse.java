package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfiguracaoResponse {
    private Long id;
    private String chave;
    private String valor;
    private String descricao;
    private String tipo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}