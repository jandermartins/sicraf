package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.VeiculoRequest;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.Veiculo;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository repository;

    @InjectMocks
    private VeiculoService service;

    private VeiculoRequest request;

    @BeforeEach
    void setUp() {
        request = new VeiculoRequest();
        request.setPlaca("ABC1234");
        request.setMarca("Toyota");
        request.setModelo("Hilux");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar veículo inexistente")
    void deveLancarExcecaoAoBuscarInexistente() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Veículo não encontrado");
    }

    @Test
    @DisplayName("Deve bloquear cadastro de veículo com placa duplicada")
    void deveBloquearPlacaDuplicada() {
        when(repository.existsByPlaca(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe veículo cadastrado com a placa ABC1234");
    }

    @Test
    @DisplayName("Deve bloquear atualização que troca placa para uma já existente")
    void deveBloquearAtualizacaoComPlacaDeOutroVeiculo() {
        Veiculo existente = Veiculo.builder().id(1L).placa("ABC1234").build();
        Veiculo outro = Veiculo.builder().id(2L).placa("XYZ9999").build();

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.findByPlaca("XYZ9999")).thenReturn(Optional.of(outro));

        request.setPlaca("XYZ9999");

        assertThatThrownBy(() -> service.atualizar(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já pertence a outro veículo");
    }
}