package br.ce.crateus.fpo.sicraf.domain.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrdemServicoResponse {
    private Long id;
    private String numeroOs;
    private Long veiculoId;
    private String veiculoPlaca;
    private String veiculoModelo;
    private Long tipoManutencaoId;
    private String tipoManutencaoNome;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataConclusao;
    private BigDecimal kmAtual;
    private String tipoOficina;
    private String oficinaNome;
    private BigDecimal valorPecas;
    private BigDecimal valorMaoObra;
    private BigDecimal valorTotal;
    private LocalDate previsaoConclusao;
    private String status;
    private String observacoes;
    private List<ItemResponse> itens;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class ItemResponse {
        private Long id;
        private String descricao;
        private String tipoItem;
        private BigDecimal quantidade;
        private BigDecimal valorUnitario;
        private BigDecimal valorTotal;
    }
}