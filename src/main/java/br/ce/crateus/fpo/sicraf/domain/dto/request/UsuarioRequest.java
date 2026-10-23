package br.ce.crateus.fpo.sicraf.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioRequest {

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    private String senha; // opcional na atualização

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Perfil é obrigatório")
    private String perfil; // GESTOR, COORDENADOR, MOTORISTA, MECANICO

    private String status;
}