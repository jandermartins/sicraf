package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.ChecklistRequest;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.model.Motorista;
import br.ce.crateus.fpo.sicraf.domain.model.Veiculo;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.ChecklistRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.MotoristaRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

    @Mock
    private ChecklistRepository checklistRepository;
    @Mock
    private VeiculoRepository veiculoRepository;
    @Mock
    private MotoristaRepository motoristaRepository;

    @InjectMocks
    private ChecklistService service;

    private Veiculo veiculoAtivo;
    private Motorista motoristaAtivo;
    private ChecklistRequest request;

    @BeforeEach
    void setUp() {
        veiculoAtivo = Veiculo.builder()
                .id(1L).placa("ABC1234").status("ATIVO").build();

        motoristaAtivo = Motorista.builder()
                .id(1L).nome("João").status("ATIVO")
                .validadeCnh(LocalDate.now().plusMonths(6))
                .build();

        request = new ChecklistRequest();
        request.setVeiculoId(1L);
        request.setMotoristaId(1L);
    }

    @Test
    @DisplayName("Deve bloquear check-list quando CNH do motorista está vencida")
    void deveBloquearQuandoCnhVencida() {
        motoristaAtivo.setValidadeCnh(LocalDate.now().minusDays(1));

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoAtivo));
        when(motoristaRepository.findById(1L)).thenReturn(Optional.of(motoristaAtivo));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CNH do motorista")
                .hasMessageContaining("está vencida");
    }

    @Test
    @DisplayName("Deve bloquear check-list quando veículo não está ATIVO")
    void deveBloquearQuandoVeiculoNaoAtivo() {
        veiculoAtivo.setStatus("MANUTENCAO");

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoAtivo));
        when(motoristaRepository.findById(1L)).thenReturn(Optional.of(motoristaAtivo));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não está ATIVO");
    }

    @Test
    @DisplayName("Deve bloquear check-list quando já existe um aberto para o veículo")
    void deveBloquearQuandoJaExisteChecklistAberto() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoAtivo));
        when(motoristaRepository.findById(1L)).thenReturn(Optional.of(motoristaAtivo));
        when(checklistRepository.existeChecklistAbertoParaVeiculo(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe um check-list em andamento");
    }
}