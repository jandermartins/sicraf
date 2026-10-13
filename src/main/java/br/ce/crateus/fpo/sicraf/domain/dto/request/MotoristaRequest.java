package br.ce.crateus.fpo.sicraf.domain.dto.request;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MotoristaRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos numéricos")
    private String cpf;

    @NotBlank(message = "CNH é obrigatória")
    @Pattern(regexp = "\\d{11}", message = "CNH deve ter 11 dígitos numéricos")
    private String cnh;

    @NotBlank(message = "Categoria da CNH é obrigatória")
    @Pattern(regexp = "[A-E]{1,2}", message = "Categoria deve ser A, B, C, D, E, AB, AC, AD, AE, etc.")
    private String categoriaCnh;

    @NotNull(message = "Validade da CNH é obrigatória")
    @Future(message = "Validade da CNH deve ser uma data futura")
    private LocalDate validadeCnh;

    @Past(message = "Data de nascimento deve ser no passado")
    private LocalDate dataNascimento;

    @Size(max = 20)
    private String telefone;

    @Email(message = "E-mail inválido")
    @Size(max = 100)
    private String email;

    @Size(max = 30)
    private String matricula;

    @Size(max = 80)
    private String secretaria;

    private LocalDate dataAdmissao;

    private String status;
    private String observacoes;
}
