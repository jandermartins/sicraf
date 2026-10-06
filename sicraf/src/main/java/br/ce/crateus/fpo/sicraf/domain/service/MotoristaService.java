package br.ce.crateus.fpo.sicraf.domain.service;


import br.ce.crateus.fpo.sicraf.domain.dto.request.MotoristaRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.MotoristaResponse;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.Motorista;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.MotoristaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MotoristaService {

    private final MotoristaRepository repository;

    public Page<MotoristaResponse> listar(String status, String secretaria, String nome, Pageable pageable) {
        return repository.findByFilters(status, secretaria, nome, pageable)
                .map(this::toResponse);
    }

    public MotoristaResponse buscarPorId(Long id) {
        Motorista motorista = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado: id=" + id));
        return toResponse(motorista);
    }

    public MotoristaResponse buscarPorCpf(String cpf) {
        Motorista motorista = repository.findByCpf(limparDocumento(cpf))
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado: CPF=" + cpf));
        return toResponse(motorista);
    }

    @Transactional
    public MotoristaResponse criar(MotoristaRequest request) {
        String cpfLimpo = limparDocumento(request.getCpf());
        String cnhLimpa = limparDocumento(request.getCnh());

        // Regra de negócio: CPF e CNH únicos
        if (repository.existsByCpf(cpfLimpo)) {
            throw new BusinessException("Já existe motorista cadastrado com o CPF " + cpfLimpo);
        }
        if (repository.existsByCnh(cnhLimpa)) {
            throw new BusinessException("Já existe motorista cadastrado com a CNH " + cnhLimpa);
        }

        // Regra de negócio: validade da CNH deve ser futura
        if (request.getValidadeCnh().isBefore(LocalDate.now())) {
            throw new BusinessException("Não é possível cadastrar motorista com CNH vencida");
        }

        request.setCpf(cpfLimpo);
        request.setCnh(cnhLimpa);
        request.setCategoriaCnh(request.getCategoriaCnh().toUpperCase());

        Motorista motorista = toEntity(request);
        Motorista salvo = repository.save(motorista);
        return toResponse(salvo);
    }

    @Transactional
    public MotoristaResponse atualizar(Long id, MotoristaRequest request) {
        Motorista motorista = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado: id=" + id));

        String cpfLimpo = limparDocumento(request.getCpf());
        String cnhLimpa = limparDocumento(request.getCnh());

        // Regra de negócio: CPF não pode pertencer a outro motorista
        repository.findByCpf(cpfLimpo).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new BusinessException("CPF " + cpfLimpo + " já pertence a outro motorista");
            }
        });

        // Regra de negócio: CNH não pode pertencer a outro motorista
        repository.findByCnh(cnhLimpa).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new BusinessException("CNH " + cnhLimpa + " já pertence a outro motorista");
            }
        });

        motorista.setNome(request.getNome());
        motorista.setCpf(cpfLimpo);
        motorista.setCnh(cnhLimpa);
        motorista.setCategoriaCnh(request.getCategoriaCnh().toUpperCase());
        motorista.setValidadeCnh(request.getValidadeCnh());
        motorista.setDataNascimento(request.getDataNascimento());
        motorista.setTelefone(request.getTelefone());
        motorista.setEmail(request.getEmail());
        motorista.setMatricula(request.getMatricula());
        motorista.setSecretaria(request.getSecretaria());
        motorista.setDataAdmissao(request.getDataAdmissao());
        motorista.setObservacoes(request.getObservacoes());
        if (request.getStatus() != null) motorista.setStatus(request.getStatus());

        return toResponse(repository.save(motorista));
    }

    @Transactional
    public void inativar(Long id) {
        Motorista motorista = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado: id=" + id));
        motorista.setStatus("INATIVO");
        repository.save(motorista);
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Motorista não encontrado: id=" + id);
        }
        repository.deleteById(id);
    }

    /**
     * RF010 - Lista motoristas com CNH vencendo nos próximos N dias (padrão: 30).
     */
    public List<MotoristaResponse> listarCnhVencendo(int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(dias);
        return repository.findCnhVencendoEm(hoje, limite).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * RF010 - Lista motoristas com CNH já vencida.
     */
    public List<MotoristaResponse> listarCnhVencida() {
        return repository.findCnhVencida(LocalDate.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------- Helpers ----------

    private String limparDocumento(String doc) {
        return doc == null ? null : doc.replaceAll("\\D", "");
    }

    private MotoristaResponse toResponse(Motorista m) {
        MotoristaResponse r = new MotoristaResponse();
        r.setId(m.getId());
        r.setNome(m.getNome());
        r.setCpf(m.getCpf());
        r.setCnh(m.getCnh());
        r.setCategoriaCnh(m.getCategoriaCnh());
        r.setValidadeCnh(m.getValidadeCnh());
        r.setDataNascimento(m.getDataNascimento());
        r.setTelefone(m.getTelefone());
        r.setEmail(m.getEmail());
        r.setMatricula(m.getMatricula());
        r.setSecretaria(m.getSecretaria());
        r.setDataAdmissao(m.getDataAdmissao());
        r.setStatus(m.getStatus());
        r.setObservacoes(m.getObservacoes());
        r.setCreatedAt(m.getCreatedAt());
        r.setUpdatedAt(m.getUpdatedAt());

        // Campos calculados
        r.setCnhVencida(m.isCnhVencida());
        r.setCnhVencendo30Dias(m.isCnhVencendoEm(30));
        if (m.getValidadeCnh() != null) {
            r.setDiasParaVencerCnh(ChronoUnit.DAYS.between(LocalDate.now(), m.getValidadeCnh()));
        }
        return r;
    }

    private Motorista toEntity(MotoristaRequest request) {
        return Motorista.builder()
                .nome(request.getNome())
                .cpf(request.getCpf())
                .cnh(request.getCnh())
                .categoriaCnh(request.getCategoriaCnh())
                .validadeCnh(request.getValidadeCnh())
                .dataNascimento(request.getDataNascimento())
                .telefone(request.getTelefone())
                .email(request.getEmail())
                .matricula(request.getMatricula())
                .secretaria(request.getSecretaria())
                .dataAdmissao(request.getDataAdmissao())
                .status(request.getStatus() != null ? request.getStatus() : "ATIVO")
                .observacoes(request.getObservacoes())
                .build();
    }
}
