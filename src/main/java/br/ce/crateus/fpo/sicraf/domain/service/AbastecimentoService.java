package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.AbastecimentoRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.AbastecimentoResponse;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.Abastecimento;
import br.ce.crateus.fpo.sicraf.domain.model.Motorista;
import br.ce.crateus.fpo.sicraf.domain.model.Veiculo;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.AbastecimentoRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.MotoristaRepository;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AbastecimentoService {

    private final AbastecimentoRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final MotoristaRepository motoristaRepository;

    public Page<AbastecimentoResponse> listar(Long veiculoId, Long motoristaId,
                                              LocalDate dataInicio, LocalDate dataFim,
                                              Pageable pageable) {
        return repository.findByFilters(veiculoId, motoristaId, dataInicio, dataFim, pageable)
                .map(this::toResponse);
    }

    public AbastecimentoResponse buscarPorId(Long id) {
        Abastecimento a = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abastecimento não encontrado: id=" + id));
        return toResponse(a);
    }

    public List<AbastecimentoResponse> listarPorVeiculo(Long veiculoId) {
        return repository.findByVeiculoIdOrderByDataDesc(veiculoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AbastecimentoResponse> listarPorMotorista(Long motoristaId) {
        return repository.findByMotoristaIdOrderByDataDesc(motoristaId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AbastecimentoResponse criar(AbastecimentoRequest request) {
        Veiculo veiculo = veiculoRepository.findById(request.getVeiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Veículo não encontrado: id=" + request.getVeiculoId()));

        Motorista motorista = null;
        if (request.getMotoristaId() != null) {
            motorista = motoristaRepository.findById(request.getMotoristaId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Motorista não encontrado: id=" + request.getMotoristaId()));
        }

        // RN01: KM não pode ser menor que o último registrado
        Abastecimento ultimo = repository.findUltimoPorVeiculo(veiculo.getId(), null).orElse(null);
        if (ultimo != null && request.getKmAtual().compareTo(ultimo.getKmAtual()) < 0) {
            throw new BusinessException(
                    "KM informado (" + request.getKmAtual() + ") é menor que o último abastecimento ("
                            + ultimo.getKmAtual() + "). Verifique o valor.");
        }

        Abastecimento abastecimento = Abastecimento.builder()
                .veiculo(veiculo)
                .motorista(motorista)
                .data(request.getData() != null ? request.getData() : LocalDate.now())
                .kmAtual(request.getKmAtual())
                .litros(request.getLitros())
                .valorTotal(request.getValorTotal())
                .tipoCombustivel(request.getTipoCombustivel())
                .posto(request.getPosto())
                .comprovanteUrl(request.getComprovanteUrl())
                .observacoes(request.getObservacoes())
                .build();

        // RN02: Calcular consumo (KM/L) se houver abastecimento anterior
        if (ultimo != null && request.getLitros().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal kmRodados = request.getKmAtual().subtract(ultimo.getKmAtual());
            if (kmRodados.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal consumo = kmRodados.divide(request.getLitros(), 2, RoundingMode.HALF_UP);
                abastecimento.setConsumoKmLitro(consumo);
            }
        }

        Abastecimento salvo = repository.save(abastecimento);

        // RN03: Atualizar KM do veículo se o KM informado for maior
        if (veiculo.getKmAtual() == null || salvo.getKmAtual().compareTo(veiculo.getKmAtual()) > 0) {
            veiculo.setKmAtual(salvo.getKmAtual());
            veiculoRepository.save(veiculo);
        }

        return toResponse(salvo);
    }

    @Transactional
    public AbastecimentoResponse atualizar(Long id, AbastecimentoRequest request) {
        Abastecimento abastecimento = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abastecimento não encontrado: id=" + id));

        Veiculo veiculo = veiculoRepository.findById(request.getVeiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Veículo não encontrado: id=" + request.getVeiculoId()));

        Motorista motorista = null;
        if (request.getMotoristaId() != null) {
            motorista = motoristaRepository.findById(request.getMotoristaId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Motorista não encontrado: id=" + request.getMotoristaId()));
        }

        abastecimento.setVeiculo(veiculo);
        abastecimento.setMotorista(motorista);
        abastecimento.setData(request.getData() != null ? request.getData() : abastecimento.getData());
        abastecimento.setKmAtual(request.getKmAtual());
        abastecimento.setLitros(request.getLitros());
        abastecimento.setValorTotal(request.getValorTotal());
        abastecimento.setTipoCombustivel(request.getTipoCombustivel());
        abastecimento.setPosto(request.getPosto());
        abastecimento.setComprovanteUrl(request.getComprovanteUrl());
        abastecimento.setObservacoes(request.getObservacoes());

        // Recalcular consumo
        Abastecimento ultimo = repository.findUltimoPorVeiculo(veiculo.getId(), id).orElse(null);
        if (ultimo != null && request.getLitros().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal kmRodados = request.getKmAtual().subtract(ultimo.getKmAtual());
            if (kmRodados.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal consumo = kmRodados.divide(request.getLitros(), 2, RoundingMode.HALF_UP);
                abastecimento.setConsumoKmLitro(consumo);
            }
        } else {
            abastecimento.setConsumoKmLitro(null);
        }

        return toResponse(repository.save(abastecimento));
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Abastecimento não encontrado: id=" + id);
        }
        repository.deleteById(id);
    }

    // ---------- Mapeamento ----------

    private AbastecimentoResponse toResponse(Abastecimento a) {
        AbastecimentoResponse r = new AbastecimentoResponse();
        r.setId(a.getId());
        r.setVeiculoId(a.getVeiculo().getId());
        r.setVeiculoPlaca(a.getVeiculo().getPlaca());
        r.setVeiculoModelo(a.getVeiculo().getModelo());
        if (a.getMotorista() != null) {
            r.setMotoristaId(a.getMotorista().getId());
            r.setMotoristaNome(a.getMotorista().getNome());
        }
        r.setData(a.getData());
        r.setKmAtual(a.getKmAtual());
        r.setLitros(a.getLitros());
        r.setValorTotal(a.getValorTotal());
        r.setPrecoPorLitro(a.getPrecoPorLitro());
        r.setConsumoKmLitro(a.getConsumoKmLitro());
        r.setTipoCombustivel(a.getTipoCombustivel());
        r.setPosto(a.getPosto());
        r.setComprovanteUrl(a.getComprovanteUrl());
        r.setObservacoes(a.getObservacoes());
        r.setCreatedAt(a.getCreatedAt());
        r.setUpdatedAt(a.getUpdatedAt());
        return r;
    }
}