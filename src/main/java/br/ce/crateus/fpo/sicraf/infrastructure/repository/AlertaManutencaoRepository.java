package br.ce.crateus.fpo.sicraf.infrastructure.repository;

import br.ce.crateus.fpo.sicraf.domain.model.AlertaManutencao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlertaManutencaoRepository extends JpaRepository<AlertaManutencao, Long> {

    @Query("""
        SELECT a FROM AlertaManutencao a
        WHERE (:status IS NULL OR a.status = :status)
          AND (:veiculoId IS NULL OR a.veiculo.id = :veiculoId)
          AND (:tipoManutencaoId IS NULL OR a.tipoManutencao.id = :tipoManutencaoId)
        ORDER BY a.createdAt DESC
    """)
    Page<AlertaManutencao> findByFilters(@Param("status") String status,
                                         @Param("veiculoId") Long veiculoId,
                                         @Param("tipoManutencaoId") Long tipoManutencaoId,
                                         Pageable pageable);

    /**
     * Busca alerta pendente para um veículo + tipo específico.
     * Usado para evitar duplicidade de alertas.
     */
    @Query("""
        SELECT a FROM AlertaManutencao a
        WHERE a.veiculo.id = :veiculoId
          AND a.tipoManutencao.id = :tipoManutencaoId
          AND a.status IN ('PENDENTE', 'AGENDADO')
    """)
    Optional<AlertaManutencao> findAtivoPorVeiculoETipo(@Param("veiculoId") Long veiculoId,
                                                        @Param("tipoManutencaoId") Long tipoManutencaoId);

    /**
     * Lista todos os alertas pendentes (dashboard).
     */
    @Query("""
        SELECT a FROM AlertaManutencao a
        WHERE a.status = 'PENDENTE'
        ORDER BY a.createdAt DESC
    """)
    List<AlertaManutencao> findPendentes();
}