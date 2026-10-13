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
@Table(name = "ocorrencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ocorrencia {

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

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "prazo_recurso")
    private LocalDate prazoRecurso;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "anexo_url", length = 255)
    private String anexoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.data == null) this.data = LocalDate.now();
        if (this.status == null) this.status = "PENDENTE";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Regra de negócio: verifica se o prazo para recurso está vencido.
     */
    @Transient
    public boolean isPrazoRecursoVencido() {
        return this.prazoRecurso != null
                && this.prazoRecurso.isBefore(LocalDate.now())
                && "PENDENTE".equalsIgnoreCase(this.status);
    }

    public enum TipoOcorrencia {
        MULTA, ACIDENTE, AVARIA, OUTRO
    }

    public enum StatusOcorrencia {
        PENDENTE, PAGA, RECORRIDA, CANCELADA
    }
}