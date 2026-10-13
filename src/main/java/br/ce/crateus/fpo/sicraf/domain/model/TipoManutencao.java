package br.ce.crateus.fpo.sicraf.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tipos_manutencao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    /**
     * Tipo da manutenção: PREVENTIVA ou CORRETIVA.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoManutencaoEnum tipo;

    /**
     * Unidade da periodicidade: KM ou MESES.
     * Obrigatória apenas se tipo = PREVENTIVA.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_periodicidade", length = 10)
    private UnidadePeriodicidade unidadePeriodicidade;

    /**
     * Valor da periodicidade (ex: 10000 para 10.000 KM, ou 6 para 6 meses).
     * Obrigatória apenas se tipo = PREVENTIVA.
     */
    @Column(name = "valor_periodicidade")
    private Integer valorPeriodicidade;

    @Column(name = "valor_estimado", precision = 10, scale = 2)
    private BigDecimal valorEstimado;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "ATIVO";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==============================
    // Enums internos
    // ==============================

    public enum TipoManutencaoEnum {
        PREVENTIVA,
        CORRETIVA
    }

    public enum UnidadePeriodicidade {
        KM,
        MESES
    }
}
