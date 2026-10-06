package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.VeiculoRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.VeiculoResponse;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.Veiculo;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository repository;

    public Page<VeiculoResponse> listar(String status, String marca, Pageable pageable) {
        return repository.findByFilters(status, marca, pageable)
                .map(this::toResponse);
    }

    public VeiculoResponse buscarPorId(Long id) {
        Veiculo veiculo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado: id=" + id));
        return toResponse(veiculo);
    }

    public VeiculoResponse buscarPorPlaca(String placa) {
        Veiculo veiculo = repository.findByPlaca(placa.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado: placa=" + placa));
        return toResponse(veiculo);
    }

    @Transactional
    public VeiculoResponse criar(VeiculoRequest request) {
        String placaNormalizada = request.getPlaca().toUpperCase().trim();

        if (repository.existsByPlaca(placaNormalizada)) {
            throw new BusinessException("Já existe veículo com a placa " + placaNormalizada);
        }
        request.setPlaca(placaNormalizada);

        Veiculo veiculo = toEntity(request);
        Veiculo salvo = repository.save(veiculo);
        return toResponse(salvo);
    }

    @Transactional
    public VeiculoResponse atualizar(Long id, VeiculoRequest request) {
        Veiculo veiculo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado: id=" + id));

        String placaNormalizada = request.getPlaca().toUpperCase().trim();
        repository.findByPlaca(placaNormalizada).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new BusinessException("Placa " + placaNormalizada + " já pertence a outro veículo");
            }
        });

        veiculo.setPlaca(placaNormalizada);
        veiculo.setRenavam(request.getRenavam());
        veiculo.setChassi(request.getChassi());
        veiculo.setMarca(request.getMarca());
        veiculo.setModelo(request.getModelo());
        veiculo.setAno(request.getAno());
        veiculo.setCor(request.getCor());
        veiculo.setTipoCombustivel(request.getTipoCombustivel());
        veiculo.setKmAtual(request.getKmAtual());
        veiculo.setCapacidadeCarga(request.getCapacidadeCarga());
        veiculo.setObservacoes(request.getObservacoes());
        if (request.getStatus() != null) veiculo.setStatus(request.getStatus());

        return toResponse(repository.save(veiculo));
    }

    @Transactional
    public void inativar(Long id) {
        Veiculo veiculo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado: id=" + id));
        veiculo.setStatus("INATIVO");
        repository.save(veiculo);
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Veículo não encontrado: id=" + id);
        }
        repository.deleteById(id);
    }

    // ---------- Mapeamentos ----------

    private VeiculoResponse toResponse(Veiculo veiculo) {
        VeiculoResponse r = new VeiculoResponse();
        r.setId(veiculo.getId());
        r.setPlaca(veiculo.getPlaca());
        r.setRenavam(veiculo.getRenavam());
        r.setChassi(veiculo.getChassi());
        r.setMarca(veiculo.getMarca());
        r.setModelo(veiculo.getModelo());
        r.setAno(veiculo.getAno());
        r.setCor(veiculo.getCor());
        r.setTipoCombustivel(veiculo.getTipoCombustivel());
        r.setKmAtual(veiculo.getKmAtual());
        r.setCapacidadeCarga(veiculo.getCapacidadeCarga());
        r.setStatus(veiculo.getStatus());
        r.setObservacoes(veiculo.getObservacoes());
        r.setCreatedAt(veiculo.getCreatedAt());
        r.setUpdatedAt(veiculo.getUpdatedAt());
        return r;
    }

    private Veiculo toEntity(VeiculoRequest request) {
        return Veiculo.builder()
                .placa(request.getPlaca())
                .renavam(request.getRenavam())
                .chassi(request.getChassi())
                .marca(request.getMarca())
                .modelo(request.getModelo())
                .ano(request.getAno())
                .cor(request.getCor())
                .tipoCombustivel(request.getTipoCombustivel())
                .kmAtual(request.getKmAtual() != null ? request.getKmAtual() : java.math.BigDecimal.ZERO)
                .capacidadeCarga(request.getCapacidadeCarga())
                .status(request.getStatus() != null ? request.getStatus() : "ATIVO")
                .observacoes(request.getObservacoes())
                .build();
    }
}
