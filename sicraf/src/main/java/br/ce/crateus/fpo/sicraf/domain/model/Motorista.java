package br.ce.crateus.fpo.sicraf.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "motoristas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Motorista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, unique = true, length = 11)
    private String cnh;

    @Column(name = "categoria_cnh", nullable = false, length = 5)
    private String categoriaCnh;

    @Column(name = "validade_cnh", nullable = false)
    private LocalDate validadeCnh;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(length = 20)
    private String telefone;

    @Column(length = 100)
    private String email;

    @Column(length = 30)
    private String matricula;

    @Column(length = 80)
    private String secretaria;

    @Column(name = "data_admissao")
    private LocalDate dataAdmissao;

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

    /**
     * Regra de negócio: verifica se a CNH está vencida na data atual.
     */
    @Transient
    public boolean isCnhVencida() {
        return this.validadeCnh != null && this.validadeCnh.isBefore(LocalDate.now());
    }

    /**
     * Regra de negócio: verifica se a CNH vence nos próximos N dias.
     */
    @Transient
    public boolean isCnhVencendoEm(int dias) {
        if (this.validadeCnh == null) return false;
        LocalDate limite = LocalDate.now().plusDays(dias);
        return !this.validadeCnh.isBefore(LocalDate.now())
                && !this.validadeCnh.isAfter(limite);
    }
}
