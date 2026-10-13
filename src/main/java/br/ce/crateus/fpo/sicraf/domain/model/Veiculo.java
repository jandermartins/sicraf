package br.ce.crateus.fpo.sicraf.domain.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "veiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 8)
    private String placa;

    @Column(length = 11)
    private String renavam;

    @Column(length = 17)
    private String chassi;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modelo;

    private Integer ano;

    @Column(length = 30)
    private String cor;

    @Column(name = "tipo_combustivel", length = 20)
    private String tipoCombustivel;

    @Column(name = "km_atual", precision = 10, scale = 2)
    private BigDecimal kmAtual;

    @Column(name = "capacidade_carga", precision = 10, scale = 2)
    private BigDecimal capacidadeCarga;

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
        if (this.kmAtual == null) this.kmAtual = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}