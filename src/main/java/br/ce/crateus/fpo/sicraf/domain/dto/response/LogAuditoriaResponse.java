package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogAuditoriaResponse {
    private Long id;
    private String usuarioEmail;
    private String acao;
    private String entidade;
    private Long entidadeId;
    private String detalhes;
    private String ipAddress;
    private LocalDateTime createdAt;
}