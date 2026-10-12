package br.ce.crateus.fpo.sicraf.infrastructure.repository;

import br.ce.crateus.fpo.sicraf.domain.model.Checklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    @Query("""
        SELECT c FROM Checklist c
        WHERE (:status IS NULL OR c.status = :status)
          AND (:veiculoId IS NULL OR c.veiculo.id = :veiculoId)
          AND (:motoristaId IS NULL OR c.motorista.id = :motoristaId)
        ORDER BY c.dataSaida DESC
    """)
    Page<Checklist> findByFilters(@Param("status") String status,
                                  @Param("veiculoId") Long veiculoId,
                                  @Param("motoristaId") Long motoristaId,
                                  Pageable pageable);

    List<Checklist> findByVeiculoIdOrderByDataSaidaDesc(Long veiculoId);

    List<Checklist> findByMotoristaIdOrderByDataSaidaDesc(Long motoristaId);

    /**
     * Verifica se há checklist EM_ANDAMENTO para o veículo.
     * Usado para impedir duplicidade de check-list aberto.
     */
    @Query("""
        SELECT COUNT(c) > 0 FROM Checklist c
        WHERE c.veiculo.id = :veiculoId
          AND c.status = 'EM_ANDAMENTO'
    """)
    boolean existeChecklistAbertoParaVeiculo(@Param("veiculoId") Long veiculoId);
}