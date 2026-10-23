package br.ce.crateus.fpo.sicraf.domain.dto.request;

import lombok.Data;

@Data
public class PreferenciaRequest {
    private Boolean notificarEmail;
    private Boolean notificarManutencao;
    private Boolean notificarCnh;
    private Boolean notificarMultas;
    private String tema;
}