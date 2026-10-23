package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.model.Motorista;
import br.ce.crateus.fpo.sicraf.domain.model.Ocorrencia;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.MotoristaRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.OcorrenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioMultasServiceTest {

    @Mock private OcorrenciaRepository ocorrenciaRepository;
    @Mock private MotoristaRepository motoristaRepository;

    @InjectMocks
    private RelatorioMultasService service;

    private Motorista joao;
    private Motorista maria;

    @BeforeEach
    void setUp() {
        joao = Motorista.builder().id(1L).nome("João").cpf("111").build();
        maria = Motorista.builder().id(2L).nome("Maria").cpf("222").build();
    }

    @Test
    @DisplayName("Deve ordenar ranking por valor total de multas (maior primeiro)")
    void deveOrdenarRankingPorValorTotal() {
        Ocorrencia multaJoao1 = Ocorrencia.builder()
                .motorista(joao).tipo("MULTA").status("PENDENTE")
                .valor(new BigDecimal("300")).data(LocalDate.now()).build();
        Ocorrencia multaJoao2 = Ocorrencia.builder()
                .motorista(joao).tipo("MULTA").status("PAGA")
                .valor(new BigDecimal("200")).data(LocalDate.now()).build();
        Ocorrencia multaMaria = Ocorrencia.builder()
                .motorista(maria).tipo("MULTA").status("PENDENTE")
                .valor(new BigDecimal("150")).data(LocalDate.now()).build();

        when(ocorrenciaRepository.findByFilters(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(multaJoao1, multaJoao2, multaMaria)));
        when(motoristaRepository.findAll()).thenReturn(List.of(joao, maria));

        var response = service.gerar(LocalDate.now().minusDays(30), LocalDate.now(), null);

        assertThat(response.getRanking()).hasSize(2);
        assertThat(response.getRanking().get(0).getNome()).isEqualTo("João");
        assertThat(response.getRanking().get(0).getValorTotal()).isEqualByComparingTo("500");
        assertThat(response.getRanking().get(1).getNome()).isEqualTo("Maria");
        assertThat(response.getRanking().get(1).getValorTotal()).isEqualByComparingTo("150");
    }
}