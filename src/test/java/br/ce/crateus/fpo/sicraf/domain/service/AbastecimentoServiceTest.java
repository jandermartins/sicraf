package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.AbastecimentoRequest;
import br.ce.crateus.fpo.sicraf.domain.model.Abastecimento;
import br.ce.crateus.fpo.sicraf.domain.model.Veiculo;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.AbastecimentoRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.MotoristaRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbastecimentoServiceTest {

    @Mock
    private AbastecimentoRepository abastecimentoRepository;
    @Mock
    private VeiculoRepository veiculoRepository;
    @Mock
    private MotoristaRepository motoristaRepository;

    @InjectMocks
    private AbastecimentoService service;

    private Veiculo veiculo;
    private AbastecimentoRequest request;

    @BeforeEach
    void setUp() {
        veiculo = Veiculo.builder()
                .id(1L).placa("ABC1234").kmAtual(new BigDecimal("45000"))
                .status("ATIVO").build();

        request = new AbastecimentoRequest();
        request.setVeiculoId(1L);
        request.setData(LocalDate.now());
        request.setKmAtual(new BigDecimal("45500"));
        request.setLitros(new BigDecimal("45.0"));
        request.setValorTotal(new BigDecimal("280.00"));
    }

    @Test
    @DisplayName("Deve calcular consumo (KM/L) no segundo abastecimento")
    void deveCalcularConsumoNoSegundoAbastecimento() {
        Abastecimento ultimo = Abastecimento.builder()
                .kmAtual(new BigDecimal("45000")).build();

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(abastecimentoRepository.findUltimoPorVeiculo(1L, null))
                .thenReturn(Optional.of(ultimo));
        when(abastecimentoRepository.save(any(Abastecimento.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var response = service.criar(request);

        // (45500 - 45000) / 45 = 500/45 = 11.11
        assertThat(response.getConsumoKmLitro())
                .isEqualByComparingTo(new BigDecimal("11.11"));
    }

    @Test
    @DisplayName("Não deve calcular consumo no primeiro abastecimento")
    void naoDeveCalcularConsumoNoPrimeiroAbastecimento() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(abastecimentoRepository.findUltimoPorVeiculo(1L, null))
                .thenReturn(Optional.empty());
        when(abastecimentoRepository.save(any(Abastecimento.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var response = service.criar(request);

        assertThat(response.getConsumoKmLitro()).isNull();
    }
}