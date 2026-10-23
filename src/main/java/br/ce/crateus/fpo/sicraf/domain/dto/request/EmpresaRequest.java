package br.ce.crateus.fpo.sicraf.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpresaRequest {

    @NotBlank(message = "Razão social é obrigatória")
    @Size(max = 150)
    private String razaoSocial;

    @Size(max = 150)
    private String nomeFantasia;

    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos numéricos")
    private String cnpj;

    @Size(max = 200)
    private String endereco;

    @Size(max = 100)
    private String cidade;

    @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas")
    private String uf;

    @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos numéricos")
    private String cep;

    @Size(max = 20)
    private String telefone;

    @Email(message = "E-mail inválido")
    @Size(max = 100)
    private String email;

    @Size(max = 100)
    private String site;

    @Size(max = 255)
    private String logoUrl;

    @Size(max = 150)
    private String responsavelLegal;
}