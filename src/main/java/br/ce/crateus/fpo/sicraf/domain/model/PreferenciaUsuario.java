package br.ce.crateus.fpo.sicraf.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "preferencias_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "notificar_email", nullable = false)
    private boolean notificarEmail = true;

    @Column(name = "notificar_manutencao", nullable = false)
    private boolean notificarManutencao = true;

    @Column(name = "notificar_cnh", nullable = false)
    private boolean notificarCnh = true;

    @Column(name = "notificar_multas", nullable = false)
    private boolean notificarMultas = true;

    @Column(nullable = false, length = 20)
    private String tema = "CLARO"; // CLARO, ESCURO

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}