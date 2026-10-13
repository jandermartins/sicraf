package br.ce.crateus.fpo.sicraf.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(
        name = "ordem_servico_itens",
        indexes = {
                @Index(name = "idx_os_itens_os", columnList = "ordem_servico_id"),
                @Index(name = "idx_os_itens_tipo", columnList = "tipo_item")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdemServicoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relacionamento N:1 com OrdemServico.
     * O cascade é controlado pelo lado pai (OrdemServico).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ordem_servico_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_os_itens_os")
    )
    private OrdemServico ordemServico;

    @Column(nullable = false, length = 255)
    private String descricao;

    /**
     * Tipo do item: PECA, MAO_DE_OBRA ou SERVICO.
     * Armazenado como String para simplicidade e legibilidade no banco.
     */
    @Column(name = "tipo_item", nullable = false, length = 20)
    private String tipoItem;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    /**
     * Calculado automaticamente: quantidade × valorUnitario.
     * Nunca deve ser setado manualmente.
     */
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    // ============================================
    // Callbacks JPA — cálculo automático
    // ============================================

    @PrePersist
    @PreUpdate
    public void calcularValorTotal() {
        if (this.quantidade == null) {
            this.quantidade = BigDecimal.ONE;
        }
        if (this.valorUnitario == null) {
            this.valorUnitario = BigDecimal.ZERO;
        }
        this.valorTotal = this.quantidade.multiply(this.valorUnitario);
    }

    // ============================================
    // Regras de negócio / utilitários
    // ============================================

    /**
     * Verifica se o item é uma peça.
     */
    @Transient
    public boolean isPeca() {
        return "PECA".equalsIgnoreCase(this.tipoItem);
    }

    /**
     * Verifica se o item é mão de obra ou serviço.
     * Usado pelo cálculo de `valorMaoObra` da OrdemServico.
     */
    @Transient
    public boolean isMaoDeObraOuServico() {
        return "MAO_DE_OBRA".equalsIgnoreCase(this.tipoItem)
                || "SERVICO".equalsIgnoreCase(this.tipoItem);
    }

    // ============================================
    // Enum de tipos válidos
    // ============================================

    public enum TipoItem {
        PECA,
        MAO_DE_OBRA,
        SERVICO;

        public static final Set<String> VALORES =
                Set.of("PECA", "MAO_DE_OBRA", "SERVICO");
    }
}