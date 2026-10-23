package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.ConfiguracaoRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.ConfiguracaoResponse;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.Configuracao;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.ConfiguracaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfiguracaoService {

    private final ConfiguracaoRepository repository;

    public Page<ConfiguracaoResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    public ConfiguracaoResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Configuração não encontrada: id=" + id));
    }

    public ConfiguracaoResponse buscarPorChave(String chave) {
        return repository.findByChave(chave)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Configuração não encontrada: chave=" + chave));
    }

    @Transactional
    public ConfiguracaoResponse criar(ConfiguracaoRequest request) {
        if (repository.existsByChave(request.getChave())) {
            throw new BusinessException("Já existe configuração com a chave: " + request.getChave());
        }

        Configuracao config = Configuracao.builder()
                .chave(request.getChave())
                .valor(request.getValor())
                .descricao(request.getDescricao())
                .tipo(request.getTipo() != null ? request.getTipo() : "STRING")
                .build();

        return toResponse(repository.save(config));
    }

    @Transactional
    public ConfiguracaoResponse atualizar(Long id, ConfiguracaoRequest request) {
        Configuracao config = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuração não encontrada: id=" + id));

        if (!config.getChave().equals(request.getChave())
                && repository.existsByChave(request.getChave())) {
            throw new BusinessException("Já existe configuração com a chave: " + request.getChave());
        }

        config.setChave(request.getChave());
        config.setValor(request.getValor());
        config.setDescricao(request.getDescricao());
        if (request.getTipo() != null) config.setTipo(request.getTipo());

        return toResponse(repository.save(config));
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Configuração não encontrada: id=" + id);
        }
        repository.deleteById(id);
    }

    private ConfiguracaoResponse toResponse(Configuracao c) {
        ConfiguracaoResponse r = new ConfiguracaoResponse();
        r.setId(c.getId());
        r.setChave(c.getChave());
        r.setValor(c.getValor());
        r.setDescricao(c.getDescricao());
        r.setTipo(c.getTipo());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}