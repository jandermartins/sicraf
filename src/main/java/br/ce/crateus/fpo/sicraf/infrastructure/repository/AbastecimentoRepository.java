package br.ce.crateus.fpo.sicraf.infrastructure.repository;

import br.ce.crateus.fpo.sicraf.domain.model.Abastecimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AbastecimentoRepository extends JpaRepository<Abastecimento, Long> {

    /**
     * Busca o último abastecimento de um veículo (para cálculo de consumo).
     * Ordenado por KM decrescente.
     */
    @Query("""
        SELECT a FROM Abastecimento a
        WHERE a.veiculo.id = :veiculoId
          AND a.id <> :excluirId
        ORDER BY a.kmAtual DESC
    """)
    List<Abastecimento> findUltimosPorVeiculo(@Param("veiculoId") Long veiculoId,
                                              @Param("excluirId") Long excluirId,
                                              Pageable pageable);

    default Optional<Abastecimento> findUltimoPorVeiculo(Long veiculoId, Long excluirId) {
        return findUltimosPorVeiculo(veiculoId, excluirId == null ? -1L : excluirId,
                Pageable.ofSize(1)).stream().findFirst();
    }

    List<Abastecimento> findByVeiculoIdOrderByDataDesc(Long veiculoId);

    List<Abastecimento> findByMotoristaIdOrderByDataDesc(Long motoristaId);

    @Query("""
    SELECT a FROM Abastecimento a
    WHERE (a.veiculo.id = :veiculoId OR :veiculoId IS NULL)
      AND (a.motorista.id = :motoristaId OR :motoristaId IS NULL)
      AND (a.data >= COALESCE(:dataInicio, a.data))
      AND (a.data <= COALESCE(:dataFim, a.data))
    ORDER BY a.data DESC
""")
    Page<Abastecimento> findByFilters(@Param("veiculoId") Long veiculoId,
                                      @Param("motoristaId") Long motoristaId,
                                      @Param("dataInicio") LocalDate dataInicio,
                                      @Param("dataFim") LocalDate dataFim,
                                      Pageable pageable);

    /**
     * Soma total de litros e valor por veículo em um período.
     * Usado pelo relatório de custo por KM (RF011).
     */
    @Query("""
        SELECT COALESCE(SUM(a.litros), 0), COALESCE(SUM(a.valorTotal), 0)
        FROM Abastecimento a
        WHERE a.veiculo.id = :veiculoId
          AND (:dataInicio IS NULL OR a.data >= :dataInicio)
          AND (:dataFim IS NULL OR a.data <= :dataFim)
    """)
    Object[] somarPorVeiculo(@Param("veiculoId") Long veiculoId,
                             @Param("dataInicio") LocalDate dataInicio,
                             @Param("dataFim") LocalDate dataFim);
}