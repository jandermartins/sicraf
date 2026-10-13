package br.ce.crateus.fpo.sicraf.infrastructure.repository;

import br.ce.crateus.fpo.sicraf.domain.model.TipoManutencao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TipoManutencaoRepository extends JpaRepository<TipoManutencao, Long> {

    Optional<TipoManutencao> findByNome(String nome);

    boolean existsByNome(String nome);

    @Query("""
    SELECT t FROM TipoManutencao t
    WHERE (t.status = :status OR :status IS NULL)
      AND (t.tipo = :tipo OR :tipo IS NULL)
      AND LOWER(t.nome) LIKE LOWER(CONCAT('%', COALESCE(:nome, ''), '%'))
    ORDER BY t.nome ASC
""")
    Page<TipoManutencao> findByFilters(@Param("status") String status,
                                       @Param("tipo") String tipo,
                                       @Param("nome") String nome,
                                       Pageable pageable);

    /**
     * Lista apenas tipos ATIVOS de manutenção preventiva.
     * Usado pelo módulo de Ordens de Serviço (RF007).
     */
    @Query("""
        SELECT t FROM TipoManutencao t
        WHERE t.status = 'ATIVO' AND t.tipo = 'PREVENTIVA'
        ORDER BY t.nome ASC
    """)
    List<TipoManutencao> findAtivosPreventivos();
}
