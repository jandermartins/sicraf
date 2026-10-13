package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.TipoManutencaoRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.TipoManutencaoResponse;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.TipoManutencao;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.TipoManutencaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoManutencaoService {

    private final TipoManutencaoRepository repository;

    public Page<TipoManutencaoResponse> listar(String status, String tipo, String nome, Pageable pageable) {
        return repository.findByFilters(status, tipo, nome, pageable)
                .map(this::toResponse);
    }

    public TipoManutencaoResponse buscarPorId(Long id) {
        TipoManutencao tipo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de manutenção não encontrado: id=" + id));
        return toResponse(tipo);
    }

    public List<TipoManutencaoResponse> listarAtivosPreventivos() {
        return repository.findAtivosPreventivos().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TipoManutencaoResponse criar(TipoManutencaoRequest request) {
        String nomeNormalizado = request.getNome().trim();

        if (repository.existsByNome(nomeNormalizado)) {
            throw new BusinessException("Já existe tipo de manutenção com o nome: " + nomeNormalizado);
        }

        TipoManutencao.TipoManutencaoEnum tipoEnum = parseTipo(request.getTipo());
        TipoManutencao.UnidadePeriodicidade unidade = parseUnidade(request.getUnidadePeriodicidade());

        // Regra de negócio: PREVENTIVA exige periodicidade
        if (tipoEnum == TipoManutencao.TipoManutencaoEnum.PREVENTIVA) {
            if (unidade == null || request.getValorPeriodicidade() == null) {
                throw new BusinessException(
                        "Manutenção PREVENTIVA exige unidade e valor de periodicidade");
            }
        }

        // Regra de negócio: CORRETIVA não deve ter periodicidade
        if (tipoEnum == TipoManutencao.TipoManutencaoEnum.CORRETIVA) {
            unidade = null;
            request.setValorPeriodicidade(null);
        }

        request.setNome(nomeNormalizado);

        TipoManutencao entity = TipoManutencao.builder()
                .nome(nomeNormalizado)
                .descricao(request.getDescricao())
                .tipo(tipoEnum)
                .unidadePeriodicidade(unidade)
                .valorPeriodicidade(request.getValorPeriodicidade())
                .valorEstimado(request.getValorEstimado())
                .status(request.getStatus() != null ? request.getStatus() : "ATIVO")
                .observacoes(request.getObservacoes())
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public TipoManutencaoResponse atualizar(Long id, TipoManutencaoRequest request) {
        TipoManutencao entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de manutenção não encontrado: id=" + id));

        String nomeNormalizado = request.getNome().trim();

        repository.findByNome(nomeNormalizado).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new BusinessException("Nome " + nomeNormalizado + " já pertence a outro tipo");
            }
        });

        TipoManutencao.TipoManutencaoEnum tipoEnum = parseTipo(request.getTipo());
        TipoManutencao.UnidadePeriodicidade unidade = parseUnidade(request.getUnidadePeriodicidade());

        if (tipoEnum == TipoManutencao.TipoManutencaoEnum.PREVENTIVA) {
            if (unidade == null || request.getValorPeriodicidade() == null) {
                throw new BusinessException(
                        "Manutenção PREVENTIVA exige unidade e valor de periodicidade");
            }
        }
        if (tipoEnum == TipoManutencao.TipoManutencaoEnum.CORRETIVA) {
            unidade = null;
            request.setValorPeriodicidade(null);
        }

        entity.setNome(nomeNormalizado);
        entity.setDescricao(request.getDescricao());
        entity.setTipo(tipoEnum);
        entity.setUnidadePeriodicidade(unidade);
        entity.setValorPeriodicidade(request.getValorPeriodicidade());
        entity.setValorEstimado(request.getValorEstimado());
        entity.setObservacoes(request.getObservacoes());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());

        return toResponse(repository.save(entity));
    }

    @Transactional
    public void inativar(Long id) {
        TipoManutencao entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de manutenção não encontrado: id=" + id));
        entity.setStatus("INATIVO");
        repository.save(entity);
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Tipo de manutenção não encontrado: id=" + id);
        }
        repository.deleteById(id);
    }

    // ---------- Helpers ----------

    private TipoManutencao.TipoManutencaoEnum parseTipo(String tipo) {
        try {
            return TipoManutencao.TipoManutencaoEnum.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Tipo inválido: " + tipo + ". Use PREVENTIVA ou CORRETIVA");
        }
    }

    private TipoManutencao.UnidadePeriodicidade parseUnidade(String unidade) {
        if (unidade == null || unidade.isBlank()) return null;
        try {
            return TipoManutencao.UnidadePeriodicidade.valueOf(unidade.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Unidade inválida: " + unidade + ". Use KM ou MESES");
        }
    }

    private TipoManutencaoResponse toResponse(TipoManutencao t) {
        TipoManutencaoResponse r = new TipoManutencaoResponse();
        r.setId(t.getId());
        r.setNome(t.getNome());
        r.setDescricao(t.getDescricao());
        r.setTipo(t.getTipo() != null ? t.getTipo().name() : null);
        r.setUnidadePeriodicidade(t.getUnidadePeriodicidade() != null
                ? t.getUnidadePeriodicidade().name() : null);
        r.setValorPeriodicidade(t.getValorPeriodicidade());
        r.setValorEstimado(t.getValorEstimado());
        r.setStatus(t.getStatus());
        r.setObservacoes(t.getObservacoes());
        r.setCreatedAt(t.getCreatedAt());
        r.setUpdatedAt(t.getUpdatedAt());
        return r;
    }
}
