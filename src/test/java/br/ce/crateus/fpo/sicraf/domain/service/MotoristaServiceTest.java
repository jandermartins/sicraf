package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.MotoristaRequest;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.model.Motorista;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.MotoristaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MotoristaServiceTest {

    @Mock
    private MotoristaRepository repository;

    @InjectMocks
    private MotoristaService service;

    private MotoristaRequest request;

    @BeforeEach
    void setUp() {
        request = new MotoristaRequest();
        request.setNome("João da Silva");
        request.setCpf("12345678901");
        request.setCnh("98765432100");
        request.setCategoriaCnh("D");
        request.setValidadeCnh(LocalDate.now().plusYears(1));
    }

    @Test
    @DisplayName("Deve bloquear cadastro com CNH vencida")
    void deveBloquearCnhVencida() {
        request.setValidadeCnh(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CNH vencida");
    }

    @Test
    @DisplayName("Deve bloquear cadastro com CPF duplicado")
    void deveBloquearCpfDuplicado() {
        when(repository.existsByCpf(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF");
    }

    @Test
    @DisplayName("Deve bloquear cadastro com CNH duplicada")
    void deveBloquearCnhDuplicada() {
        when(repository.existsByCpf(anyString())).thenReturn(false);
        when(repository.existsByCnh(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CNH");
    }
}