package br.ce.crateus.fpo.sicraf.infrastructure.repository;

import br.ce.crateus.fpo.sicraf.domain.model.Ocorrencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    @Query("""
    SELECT o FROM Ocorrencia o
    WHERE (o.tipo = :tipo OR :tipo IS NULL)
      AND (o.status = :status OR :status IS NULL)
      AND (o.veiculo.id = :veiculoId OR :veiculoId IS NULL)
      AND (o.motorista.id = :motoristaId OR :motoristaId IS NULL)
      AND (o.data >= COALESCE(:dataInicio, o.data))
      AND (o.data <= COALESCE(:dataFim, o.data))
    ORDER BY o.data DESC
""")
    Page<Ocorrencia> findByFilters(@Param("tipo") String tipo,
                                   @Param("status") String status,
                                   @Param("veiculoId") Long veiculoId,
                                   @Param("motoristaId") Long motoristaId,
                                   @Param("dataInicio") LocalDate dataInicio,
                                   @Param("dataFim") LocalDate dataFim,
                                   Pageable pageable);

    List<Ocorrencia> findByVeiculoIdOrderByDataDesc(Long veiculoId);

    List<Ocorrencia> findByMotoristaIdOrderByDataDesc(Long motoristaId);

    /**
     * RF012 - Contagem e soma de multas por motorista.
     */
    @Query("""
        SELECT COUNT(o), COALESCE(SUM(o.valor), 0)
        FROM Ocorrencia o
        WHERE o.motorista.id = :motoristaId
          AND o.tipo = 'MULTA'
          AND (:dataInicio IS NULL OR o.data >= :dataInicio)
          AND (:dataFim IS NULL OR o.data <= :dataFim)
    """)
    Object[] somarMultasPorMotorista(@Param("motoristaId") Long motoristaId,
                                     @Param("dataInicio") LocalDate dataInicio,
                                     @Param("dataFim") LocalDate dataFim);
}