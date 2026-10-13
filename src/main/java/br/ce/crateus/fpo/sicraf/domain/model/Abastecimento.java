package br.ce.crateus.fpo.sicraf.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "abastecimentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Abastecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id")
    private Motorista motorista;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "km_atual", nullable = false, precision = 10, scale = 2)
    private BigDecimal kmAtual;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal litros;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "tipo_combustivel", length = 20)
    private String tipoCombustivel;

    @Column(length = 100)
    private String posto;

    /**
     * Consumo calculado (KM/L) — preenchido automaticamente pelo Service
     * com base no último abastecimento do mesmo veículo.
     */
    @Column(name = "consumo_km_litro", precision = 10, scale = 2)
    private BigDecimal consumoKmLitro;

    @Column(name = "comprovante_url", length = 255)
    private String comprovanteUrl;

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
        if (this.data == null) this.data = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Retorna o preço por litro (R$/L) calculado.
     */
    @Transient
    public BigDecimal getPrecoPorLitro() {
        if (litros == null || litros.compareTo(BigDecimal.ZERO) == 0) return null;
        return valorTotal.divide(litros, 2, java.math.RoundingMode.HALF_UP);
    }
}