package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsuarioResponse {
    private Long id;
    private String email;
    private String nome;
    private String perfil;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime ultimoAcesso;
}