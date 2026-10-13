package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.response.HistoricoEventoResponse;
import br.ce.crateus.fpo.sicraf.domain.dto.response.HistoricoVeiculoResponse;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.*;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricoVeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ChecklistRepository checklistRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    /**
     * Retorna o histórico completo de um veículo, com timeline e resumo.
     *
     * @param veiculoId ID do veículo
     * @param dataInicio (opcional) filtra eventos a partir desta data
     * @param dataFim (opcional) filtra eventos até esta data
     * @param tipoEvento (opcional) filtra por tipo: CHECKLIST, ABASTECIMENTO, OCORRENCIA, ORDEM_SERVICO
     */
    public HistoricoVeiculoResponse obterHistorico(Long veiculoId,
                                                   LocalDate dataInicio,
                                                   LocalDate dataFim,
                                                   String tipoEvento) {

        // 1. Buscar o veículo
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Veículo não encontrado: id=" + veiculoId));

        // 2. Buscar todos os eventos
        List<HistoricoEventoResponse> eventos = new ArrayList<>();

        // 2.1 Checklists
        if (deveIncluir(tipoEvento, "CHECKLIST")) {
            checklistRepository.findByVeiculoIdOrderByDataSaidaDesc(veiculoId).stream()
                    .filter(c -> dentroDoPeriodo(c.getDataSaida(), dataInicio, dataFim))
                    .forEach(c -> eventos.add(toChecklistEvento(c)));
        }

        // 2.2 Abastecimentos
        if (deveIncluir(tipoEvento, "ABASTECIMENTO")) {
            abastecimentoRepository.findByVeiculoIdOrderByDataDesc(veiculoId).stream()
                    .filter(a -> dentroDoPeriodo(a.getData(), dataInicio, dataFim))
                    .forEach(a -> eventos.add(toAbastecimentoEvento(a)));
        }

        // 2.3 Ocorrências
        if (deveIncluir(tipoEvento, "OCORRENCIA")) {
            ocorrenciaRepository.findByVeiculoIdOrderByDataDesc(veiculoId).stream()
                    .filter(o -> dentroDoPeriodo(o.getData(), dataInicio, dataFim))
                    .forEach(o -> eventos.add(toOcorrenciaEvento(o)));
        }

        // 2.4 Ordens de Serviço
        if (deveIncluir(tipoEvento, "ORDEM_SERVICO")) {
            ordemServicoRepository.findByVeiculoIdOrderByDataAberturaDesc(veiculoId).stream()
                    .filter(os -> dentroDoPeriodo(os.getDataAbertura(), dataInicio, dataFim))
                    .forEach(os -> eventos.add(toOrdemServicoEvento(os)));
        }

        // 3. Ordenar a timeline (mais recente primeiro)
        eventos.sort(Comparator.comparing(HistoricoEventoResponse::getData).reversed());

        // 4. Montar resposta
        HistoricoVeiculoResponse response = new HistoricoVeiculoResponse();
        response.setVeiculoId(veiculo.getId());
        response.setPlaca(veiculo.getPlaca());
        response.setMarca(veiculo.getMarca());
        response.setModelo(veiculo.getModelo());
        response.setAno(veiculo.getAno());
        response.setKmAtual(veiculo.getKmAtual());
        response.setStatus(veiculo.getStatus());

        response.setEventos(eventos);
        calcularResumo(response, veiculoId, dataInicio, dataFim);

        return response;
    }

    // ============================================
    // Conversores de entidades → eventos
    // ============================================

    private HistoricoEventoResponse toChecklistEvento(Checklist c) {
        long pendentes = c.getItens().stream().filter(i -> !i.isVerificado()).count();
        String descricao = c.getItens().size() + " itens verificados";
        if (pendentes > 0) {
            descricao += " (" + pendentes + " pendentes)";
        }

        return HistoricoEventoResponse.builder()
                .tipo("CHECKLIST")
                .data(c.getDataSaida())
                .titulo("Check-list de Saída")
                .descricao(descricao)
                .km(c.getKmSaida())
                .status(c.getStatus())
                .referenciaId(c.getId())
                .build();
    }

    private HistoricoEventoResponse toAbastecimentoEvento(Abastecimento a) {
        String descricao = a.getLitros() + " L";
        if (a.getConsumoKmLitro() != null) {
            descricao += " • Consumo: " + a.getConsumoKmLitro() + " km/L";
        }
        if (a.getPosto() != null && !a.getPosto().isBlank()) {
            descricao += " • " + a.getPosto();
        }

        return HistoricoEventoResponse.builder()
                .tipo("ABASTECIMENTO")
                .data(a.getData() != null ? a.getData().atStartOfDay() : null)
                .titulo("Abastecimento")
                .descricao(descricao)
                .valor(a.getValorTotal())
                .km(a.getKmAtual())
                .status("REGISTRADO")
                .referenciaId(a.getId())
                .build();
    }

    private HistoricoEventoResponse toOcorrenciaEvento(Ocorrencia o) {
        String descricao = o.getDescricao();
        if (o.getPrazoRecurso() != null && "PENDENTE".equalsIgnoreCase(o.getStatus())) {
            descricao += " • Prazo recurso: " + o.getPrazoRecurso();
        }

        return HistoricoEventoResponse.builder()
                .tipo("OCORRENCIA")
                .data(o.getData() != null ? o.getData().atStartOfDay() : null)
                .titulo(o.getTipo())
                .descricao(descricao)
                .valor(o.getValor())
                .status(o.getStatus())
                .referenciaId(o.getId())
                .build();
    }

    private HistoricoEventoResponse toOrdemServicoEvento(OrdemServico os) {
        StringBuilder sb = new StringBuilder();
        sb.append(os.getItens().size()).append(" itens");
        if (os.getOficinaNome() != null && !os.getOficinaNome().isBlank()) {
            sb.append(" • ").append(os.getOficinaNome());
        } else {
            sb.append(" • Oficina ").append(os.getTipoOficina());
        }

        String titulo = os.getTipoManutencao() != null
                ? os.getTipoManutencao().getNome()
                : "Ordem de Serviço " + os.getNumeroOs();

        return HistoricoEventoResponse.builder()
                .tipo("ORDEM_SERVICO")
                .data(os.getDataAbertura())
                .titulo(titulo)
                .descricao(sb.toString())
                .valor(os.getValorTotal())
                .km(os.getKmAtual())
                .status(os.getStatus())
                .referenciaId(os.getId())
                .build();
    }

    // ============================================
    // Cálculo do resumo
    // ============================================

    private void calcularResumo(HistoricoVeiculoResponse response, Long veiculoId,
                                LocalDate dataInicio, LocalDate dataFim) {

        List<Checklist> checklists = checklistRepository.findByVeiculoIdOrderByDataSaidaDesc(veiculoId);
        List<Abastecimento> abastecimentos = abastecimentoRepository.findByVeiculoIdOrderByDataDesc(veiculoId);
        List<Ocorrencia> ocorrencias = ocorrenciaRepository.findByVeiculoIdOrderByDataDesc(veiculoId);
        List<OrdemServico> ordensServico = ordemServicoRepository.findByVeiculoIdOrderByDataAberturaDesc(veiculoId);

        // Filtrar por período
        checklists = checklists.stream().filter(c -> dentroDoPeriodo(c.getDataSaida(), dataInicio, dataFim)).toList();
        abastecimentos = abastecimentos.stream().filter(a -> dentroDoPeriodo(a.getData(), dataInicio, dataFim)).toList();
        ocorrencias = ocorrencias.stream().filter(o -> dentroDoPeriodo(o.getData(), dataInicio, dataFim)).toList();
        ordensServico = ordensServico.stream().filter(os -> dentroDoPeriodo(os.getDataAbertura(), dataInicio, dataFim)).toList();

        response.setTotalChecklists((long) checklists.size());
        response.setTotalAbastecimentos((long) abastecimentos.size());
        response.setTotalOcorrencias((long) ocorrencias.size());
        response.setTotalOrdensServico((long) ordensServico.size());
        response.setTotalEventos((long) (checklists.size() + abastecimentos.size()
                + ocorrencias.size() + ordensServico.size()));

        // Totais de combustível
        BigDecimal totalLitros = abastecimentos.stream()
                .map(Abastecimento::getLitros)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCombustivel = abastecimentos.stream()
                .map(Abastecimento::getValorTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Totais de manutenção (apenas OS CONCLUIDAS)
        BigDecimal totalManutencao = ordensServico.stream()
                .filter(os -> "CONCLUIDA".equalsIgnoreCase(os.getStatus()))
                .map(OrdemServico::getValorTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Totais de ocorrências (apenas PAGAS)
        BigDecimal totalOcorrencias = ocorrencias.stream()
                .filter(o -> "PAGA".equalsIgnoreCase(o.getStatus()))
                .map(Ocorrencia::getValor)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setTotalLitros(totalLitros);
        response.setTotalGastoCombustivel(totalCombustivel);
        response.setTotalGastoManutencao(totalManutencao);
        response.setTotalGastoOcorrencias(totalOcorrencias);
        response.setTotalGastoGeral(totalCombustivel.add(totalManutencao).add(totalOcorrencias));

        // Consumo médio (KM/L) = soma de KM rodados / soma de litros
        // Simplificado: usar km do último abastecimento - km do primeiro
        if (abastecimentos.size() >= 2 && totalLitros.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal kmMaisRecente = abastecimentos.get(0).getKmAtual();
            BigDecimal kmMaisAntigo = abastecimentos.get(abastecimentos.size() - 1).getKmAtual();

            if (kmMaisRecente != null && kmMaisAntigo != null) {
                BigDecimal kmRodados = kmMaisRecente.subtract(kmMaisAntigo);
                if (kmRodados.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal consumoMedio = kmRodados.divide(totalLitros, 2, RoundingMode.HALF_UP);
                    response.setConsumoMedioKmLitro(consumoMedio);
                }
            }
        }
    }

    // ============================================
    // Helpers
    // ============================================

    private boolean deveIncluir(String tipoFiltro, String tipoEvento) {
        return tipoFiltro == null || tipoFiltro.isBlank()
                || tipoFiltro.equalsIgnoreCase(tipoEvento);
    }

    private boolean dentroDoPeriodo(LocalDateTime dataHora, LocalDate inicio, LocalDate fim) {
        if (dataHora == null) return true;
        LocalDate data = dataHora.toLocalDate();
        return dentroDoPeriodo(data, inicio, fim);
    }

    private boolean dentroDoPeriodo(LocalDate data, LocalDate inicio, LocalDate fim) {
        if (data == null) return true;
        if (inicio != null && data.isBefore(inicio)) return false;
        if (fim != null && data.isAfter(fim)) return false;
        return true;
    }
}