package br.ce.crateus.fpo.sicraf.infrastructure.repository;

import br.ce.crateus.fpo.sicraf.domain.model.LogAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {

    @Query("""
    SELECT l FROM LogAuditoria l
    WHERE (l.acao = :acao OR :acao IS NULL)
      AND (l.entidade = :entidade OR :entidade IS NULL)
      AND LOWER(l.usuarioEmail) LIKE LOWER(CONCAT('%', COALESCE(:usuarioEmail, ''), '%'))
      AND (l.createdAt >= COALESCE(:dataInicio, l.createdAt))
      AND (l.createdAt <= COALESCE(:dataFim, l.createdAt))
    ORDER BY l.createdAt DESC
""")
    Page<LogAuditoria> findByFilters(@Param("acao") String acao,
                                     @Param("entidade") String entidade,
                                     @Param("usuarioEmail") String usuarioEmail,
                                     @Param("dataInicio") LocalDateTime dataInicio,
                                     @Param("dataFim") LocalDateTime dataFim,
                                     Pageable pageable);
}