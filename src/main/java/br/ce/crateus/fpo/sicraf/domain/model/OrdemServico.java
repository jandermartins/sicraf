package br.ce.crateus.fpo.sicraf.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordens_servico")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_os", nullable = false, unique = true, length = 20)
    private String numeroOs;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_manutencao_id")
    private TipoManutencao tipoManutencao;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(name = "km_atual", precision = 10, scale = 2)
    private BigDecimal kmAtual;

    @Column(name = "tipo_oficina", nullable = false, length = 20)
    private String tipoOficina;

    @Column(name = "oficina_nome", length = 100)
    private String oficinaNome;

    @Column(name = "valor_pecas", precision = 10, scale = 2)
    private BigDecimal valorPecas;

    @Column(name = "valor_mao_obra", precision = 10, scale = 2)
    private BigDecimal valorMaoObra;

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "previsao_conclusao")
    private LocalDate previsaoConclusao;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrdemServicoItem> itens = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.dataAbertura == null) this.dataAbertura = LocalDateTime.now();
        if (this.status == null) this.status = "ABERTA";
        if (this.tipoOficina == null) this.tipoOficina = "INTERNA";
        if (this.valorPecas == null) this.valorPecas = BigDecimal.ZERO;
        if (this.valorMaoObra == null) this.valorMaoObra = BigDecimal.ZERO;
        if (this.valorTotal == null) this.valorTotal = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void adicionarItem(OrdemServicoItem item) {
        item.setOrdemServico(this);
        this.itens.add(item);
    }

    /**
     * Recalcula os totais (peças, mão de obra e total geral) com base nos itens.
     */
    public void recalcularTotais() {
        BigDecimal pecas = BigDecimal.ZERO;
        BigDecimal maoObra = BigDecimal.ZERO;

        for (OrdemServicoItem item : itens) {
            if ("PECA".equalsIgnoreCase(item.getTipoItem())) {
                pecas = pecas.add(item.getValorTotal());
            } else if ("MAO_DE_OBRA".equalsIgnoreCase(item.getTipoItem())
                    || "SERVICO".equalsIgnoreCase(item.getTipoItem())) {
                maoObra = maoObra.add(item.getValorTotal());
            }
        }

        this.valorPecas = pecas;
        this.valorMaoObra = maoObra;
        this.valorTotal = pecas.add(maoObra);
    }

    public enum StatusOS {
        ABERTA, EM_ANDAMENTO, AGUARDANDO_PECAS, CONCLUIDA, CANCELADA
    }

    public enum TipoOficina {
        INTERNA, EXTERNA
    }
}