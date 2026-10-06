package br.ce.crateus.fpo.sicraf.infrastructure.repository;

import br.ce.crateus.fpo.sicraf.domain.model.Motorista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MotoristaRepository extends JpaRepository<Motorista, Long> {

    Optional<Motorista> findByCpf(String cpf);

    Optional<Motorista> findByCnh(String cnh);

    boolean existsByCpf(String cpf);

    boolean existsByCnh(String cnh);

    @Query("""
        SELECT m FROM Motorista m
        WHERE (:status IS NULL OR m.status = :status)
          AND (:secretaria IS NULL OR LOWER(m.secretaria) LIKE LOWER(CONCAT('%', :secretaria, '%')))
          AND (:nome IS NULL OR LOWER(m.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
    """)
    Page<Motorista> findByFilters(@Param("status") String status,
                                  @Param("secretaria") String secretaria,
                                  @Param("nome") String nome,
                                  Pageable pageable);

    /**
     * RF010 - Busca motoristas com CNH vencendo nos próximos N dias.
     */
    @Query("""
        SELECT m FROM Motorista m
        WHERE m.validadeCnh BETWEEN :hoje AND :limite
        ORDER BY m.validadeCnh ASC
    """)
    List<Motorista> findCnhVencendoEm(@Param("hoje") LocalDate hoje,
                                      @Param("limite") LocalDate limite);

    /**
     * RF010 - Busca motoristas com CNH já vencida.
     */
    @Query("""
        SELECT m FROM Motorista m
        WHERE m.validadeCnh < :hoje
        ORDER BY m.validadeCnh ASC
    """)
    List<Motorista> findCnhVencida(@Param("hoje") LocalDate hoje);
}
