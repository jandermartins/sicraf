package br.ce.crateus.fpo.sicraf.infrastructure.repository;

import br.ce.crateus.fpo.sicraf.domain.model.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Optional<Veiculo> findByPlaca(String placa);

    boolean existsByPlaca(String placa);

    @Query("""
        SELECT v FROM Veiculo v
        WHERE (:status IS NULL OR v.status = :status)
          AND (:marca IS NULL OR LOWER(v.marca) LIKE LOWER(CONCAT('%', :marca, '%')))
    """)
    Page<Veiculo> findByFilters(@Param("status") String status,
                                @Param("marca") String marca,
                                Pageable pageable);
}
