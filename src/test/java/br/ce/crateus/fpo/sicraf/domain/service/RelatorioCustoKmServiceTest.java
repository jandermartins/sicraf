package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.model.Abastecimento;
import br.ce.crateus.fpo.sicraf.domain.model.Veiculo;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioCustoKmServiceTest {

    @Mock private VeiculoRepository veiculoRepository;
    @Mock private AbastecimentoRepository abastecimentoRepository;
    @Mock private OcorrenciaRepository ocorrenciaRepository;
    @Mock private OrdemServicoRepository ordemServicoRepository;

    @InjectMocks
    private RelatorioCustoKmService service;

    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        veiculo = Veiculo.builder()
                .id(1L).placa("ABC1234").marca("Toyota").modelo("Hilux")
                .status("ATIVO").build();
    }

    @Test
    @DisplayName("Deve calcular custo por KM corretamente")
    void deveCalcularCustoPorKm() {
        Abastecimento a1 = Abastecimento.builder()
                .veiculo(veiculo).kmAtual(new BigDecimal("45000"))
                .litros(new BigDecimal("40"))
                .valorTotal(new BigDecimal("280"))
                .build();

        Abastecimento a2 = Abastecimento.builder()
                .veiculo(veiculo).kmAtual(new BigDecimal("45500"))
                .litros(new BigDecimal("40"))
                .valorTotal(new BigDecimal("280"))
                .build();

        when(veiculoRepository.findAll()).thenReturn(List.of(veiculo));
        when(abastecimentoRepository.findByFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a1, a2)));
        when(ocorrenciaRepository.findByFilters(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(ordemServicoRepository.findAll()).thenReturn(List.of());

        var response = service.gerar(LocalDate.now().minusDays(30), LocalDate.now(), null);

        // Custo total = 280 + 280 = 560. KM rodado = 500. Custo/KM = 1.12
        assertThat(response.getCustoTotalFrota()).isEqualByComparingTo("560.00");
        assertThat(response.getKmTotalFrota()).isEqualByComparingTo("500.00");
        assertThat(response.getCustoMedioPorKmFrota()).isEqualByComparingTo("1.1200");
    }

    @Test
    @DisplayName("Deve retornar custo/KM zero quando não há KM rodado")
    void deveRetornarZeroSemKmRodado() {
        when(veiculoRepository.findAll()).thenReturn(List.of(veiculo));
        when(abastecimentoRepository.findByFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(ocorrenciaRepository.findByFilters(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(ordemServicoRepository.findAll()).thenReturn(List.of());

        var response = service.gerar(LocalDate.now().minusDays(30), LocalDate.now(), null);

        assertThat(response.getCustoMedioPorKmFrota()).isEqualByComparingTo("0");
    }
}