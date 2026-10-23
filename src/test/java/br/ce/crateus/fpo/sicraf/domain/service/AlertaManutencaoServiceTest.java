package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.model.AlertaManutencao;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaManutencaoServiceTest {

    @Mock private AlertaManutencaoRepository repository;
    @Mock private VeiculoRepository veiculoRepository;
    @Mock private TipoManutencaoRepository tipoManutencaoRepository;
    @Mock private OrdemServicoRepository ordemServicoRepository;

    @InjectMocks
    private AlertaManutencaoService service;

    @Test
    @DisplayName("Deve bloquear cancelamento de alerta já concluído")
    void deveBloquearCancelamentoDeAlertaConcluido() {
        AlertaManutencao alerta = AlertaManutencao.builder()
                .id(1L).status("CONCLUIDO").build();

        when(repository.findById(1L)).thenReturn(Optional.of(alerta));

        assertThatThrownBy(() -> service.cancelar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já está concluído");
    }

    @Test
    @DisplayName("Deve bloquear agendamento de alerta que não está PENDENTE")
    void deveBloquearAgendamentoDeAlertaNaoPendente() {
        AlertaManutencao alerta = AlertaManutencao.builder()
                .id(1L).status("CANCELADO").build();

        when(repository.findById(1L)).thenReturn(Optional.of(alerta));

        assertThatThrownBy(() -> service.agendar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Só é possível agendar alertas com status PENDENTE");
    }
}