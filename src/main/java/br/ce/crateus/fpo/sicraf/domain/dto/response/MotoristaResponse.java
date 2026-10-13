package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MotoristaResponse {
    private Long id;
    private String nome;
    private String cpf;
    private String cnh;
    private String categoriaCnh;
    private LocalDate validadeCnh;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private String matricula;
    private String secretaria;
    private LocalDate dataAdmissao;
    private String status;
    private String observacoes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Campos calculados (regras de negócio)
    private Boolean cnhVencida;
    private Boolean cnhVencendo30Dias;
    private Long diasParaVencerCnh;
}
