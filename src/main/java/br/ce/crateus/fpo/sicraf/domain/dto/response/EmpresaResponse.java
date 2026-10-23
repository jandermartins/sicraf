package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmpresaResponse {
    private Long id;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String endereco;
    private String cidade;
    private String uf;
    private String cep;
    private String telefone;
    private String email;
    private String site;
    private String logoUrl;
    private String responsavelLegal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}