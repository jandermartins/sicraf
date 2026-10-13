package br.ce.crateus.fpo.sicraf.infrastructure.repository;

import br.ce.crateus.fpo.sicraf.domain.model.OrdemServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    Optional<OrdemServico> findByNumeroOs(String numeroOs);

    @Query("""
        SELECT o FROM OrdemServico o
        WHERE (:status IS NULL OR o.status = :status)
          AND (:veiculoId IS NULL OR o.veiculo.id = :veiculoId)
          AND (:tipoOficina IS NULL OR o.tipoOficina = :tipoOficina)
        ORDER BY o.dataAbertura DESC
    """)
    Page<OrdemServico> findByFilters(@Param("status") String status,
                                     @Param("veiculoId") Long veiculoId,
                                     @Param("tipoOficina") String tipoOficina,
                                     Pageable pageable);

    List<OrdemServico> findByVeiculoIdOrderByDataAberturaDesc(Long veiculoId);

    /**
     * Verifica se existe OS em aberto (não concluída nem cancelada) para o veículo.
     * Usado pela regra RN01 do check-list.
     */
    @Query("""
        SELECT COUNT(o) > 0 FROM OrdemServico o
        WHERE o.veiculo.id = :veiculoId
          AND o.status IN ('ABERTA', 'EM_ANDAMENTO', 'AGUARDANDO_PECAS')
    """)
    boolean existeOsAbertaParaVeiculo(@Param("veiculoId") Long veiculoId);

    /**
     * Gera o próximo número de OS (sequencial por ano).
     */
    @Query("""
        SELECT COALESCE(MAX(CAST(SUBSTRING(o.numeroOs, 9) AS integer)), 0)
        FROM OrdemServico o
        WHERE o.numeroOs LIKE CONCAT('OS-', :ano, '-%')
    """)
    Integer proximoNumeroSequencial(@Param("ano") int ano);

    Optional<OrdemServico> findTopByVeiculoIdAndTipoManutencaoIdAndStatusOrderByDataConclusaoDesc(
            Long veiculoId, Long tipoManutencaoId, String status);
}