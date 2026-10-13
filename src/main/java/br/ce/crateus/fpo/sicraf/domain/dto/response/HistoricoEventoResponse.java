package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa um evento individual na timeline do veículo.
 * Pode ser originado de: CHECKLIST, ABASTECIMENTO, OCORRENCIA ou ORDEM_SERVICO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoEventoResponse {

    /** Tipo do evento: CHECKLIST, ABASTECIMENTO, OCORRENCIA, ORDEM_SERVICO */
    private String tipo;

    /** Data/hora do evento (para ordenação na timeline) */
    private LocalDateTime data;

    /** Título curto (ex: "Troca de Óleo", "Abastecimento", "Multa") */
    private String titulo;

    /** Descrição detalhada do evento */
    private String descricao;

    /** Valor monetário associado (se houver) */
    private BigDecimal valor;

    /** KM do veículo no momento do evento */
    private BigDecimal km;

    /** Status do evento (varia por tipo) */
    private String status;

    /** ID do registro original (para consulta detalhada) */
    private Long referenciaId;
}