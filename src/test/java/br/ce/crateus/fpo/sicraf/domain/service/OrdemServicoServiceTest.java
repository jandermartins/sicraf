package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.OrdemServicoRequest;
import br.ce.crateus.fpo.sicraf.domain.model.OrdemServico;
import br.ce.crateus.fpo.sicraf.domain.model.Veiculo;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock private OrdemServicoRepository osRepository;
    @Mock private VeiculoRepository veiculoRepository;
    @Mock private TipoManutencaoRepository tipoManutencaoRepository;
    @Mock private AlertaManutencaoService alertaService;

    @InjectMocks
    private OrdemServicoService service;

    private Veiculo veiculo;
    private OrdemServicoRequest request;

    @BeforeEach
    void setUp() {
        veiculo = Veiculo.builder()
                .id(1L).placa("ABC1234").kmAtual(new BigDecimal("46000"))
                .status("ATIVO").build();

        OrdemServicoRequest.ItemRequest item1 = new OrdemServicoRequest.ItemRequest();
        item1.setDescricao("Óleo 15W40");
        item1.setTipoItem("PECA");
        item1.setQuantidade(new BigDecimal("4"));
        item1.setValorUnitario(new BigDecimal("45.00"));

        OrdemServicoRequest.ItemRequest item2 = new OrdemServicoRequest.ItemRequest();
        item2.setDescricao("Mão de obra");
        item2.setTipoItem("MAO_DE_OBRA");
        item2.setQuantidade(BigDecimal.ONE);
        item2.setValorUnitario(new BigDecimal("80.00"));

        request = new OrdemServicoRequest();
        request.setVeiculoId(1L);
        request.setTipoOficina("INTERNA");
        request.setItens(List.of(item1, item2));
    }

    @Test
    @DisplayName("Deve gerar número de OS sequencial no formato OS-ANO-0001")
    void deveGerarNumeroOsSequencial() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(osRepository.proximoNumeroSequencial(anyInt())).thenReturn(0);
        when(osRepository.existeOsAbertaParaVeiculo(1L)).thenReturn(false);
        when(osRepository.save(any(OrdemServico.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var response = service.criar(request);

        assertThat(response.getNumeroOs()).matches("OS-\\d{4}-0001");
    }

    @Test
    @DisplayName("Deve calcular valor total da OS somando peças e mão de obra")
    void deveCalcularValorTotal() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(osRepository.proximoNumeroSequencial(anyInt())).thenReturn(0);
        when(osRepository.existeOsAbertaParaVeiculo(1L)).thenReturn(false);
        when(osRepository.save(any(OrdemServico.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var response = service.criar(request);

        // Peças: 4 * 45 = 180, Mão de obra: 80 → Total = 260
        assertThat(response.getValorPecas()).isEqualByComparingTo("180.00");
        assertThat(response.getValorMaoObra()).isEqualByComparingTo("80.00");
        assertThat(response.getValorTotal()).isEqualByComparingTo("260.00");
    }
}