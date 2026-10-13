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
@Table(
        name = "alertas_manutencao",
        indexes = {
                @Index(name = "idx_alertas_veiculo", columnList = "veiculo_id"),
                @Index(name = "idx_alertas_status", columnList = "status"),
                @Index(name = "idx_alertas_notificado", columnList = "notificado")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_manutencao_id", nullable = false)
    private TipoManutencao tipoManutencao;

    @Column(name = "km_ultima_manutencao", precision = 10, scale = 2)
    private BigDecimal kmUltimaManutencao;

    @Column(name = "data_ultima_manutencao")
    private LocalDate dataUltimaManutencao;

    @Column(name = "km_proxima_manutencao", precision = 10, scale = 2)
    private BigDecimal kmProximaManutencao;

    @Column(name = "data_proxima_manutencao")
    private LocalDate dataProximaManutencao;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private boolean notificado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "PENDENTE";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calcula quantos KM faltam para a próxima manutenção.
     */
    @Transient
    public BigDecimal getKmRestantes() {
        if (kmProximaManutencao == null || veiculo == null || veiculo.getKmAtual() == null) {
            return null;
        }
        return kmProximaManutencao.subtract(veiculo.getKmAtual());
    }

    /**
     * Calcula quantos dias faltam para a próxima manutenção.
     */
    @Transient
    public Long getDiasRestantes() {
        if (dataProximaManutencao == null) return null;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dataProximaManutencao);
    }

    public enum StatusAlerta {
        PENDENTE, AGENDADO, CONCLUIDO, CANCELADO
    }
}