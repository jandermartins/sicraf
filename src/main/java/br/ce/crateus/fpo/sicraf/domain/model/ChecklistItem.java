package br.ce.crateus.fpo.sicraf.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "checklist_itens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @Column(nullable = false, length = 80)
    private String item;

    @Column(nullable = false)
    private boolean verificado;

    @Column(length = 255)
    private String observacao;

    /**
     * Lista fixa de itens obrigatórios do check-list (RN do SICRAF).
     * Ordem importa — o frontend exibe nesta sequência.
     */
    public static final java.util.List<String> ITENS_PADRAO = java.util.List.of(
            "Nível de Óleo",
            "Pressão dos Pneus",
            "Luzes (faróis, lanternas)",
            "Nível de Combustível",
            "Documentos (CRLV, CNH)",
            "Limpeza do Veículo"
    );
}