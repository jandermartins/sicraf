package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.UsuarioRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.UsuarioResponse;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.Usuario;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final Set<String> PERFIS_VALIDOS =
            Set.of("GESTOR", "COORDENADOR", "MOTORISTA", "MECANICO");

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public Page<UsuarioResponse> listar(String perfil, String status, Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public UsuarioResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: id=" + id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Já existe usuário com o e-mail: " + request.getEmail());
        }

        String perfil = request.getPerfil().toUpperCase();
        if (!PERFIS_VALIDOS.contains(perfil)) {
            throw new BusinessException("Perfil inválido: " + request.getPerfil());
        }

        if (request.getSenha() == null || request.getSenha().isBlank()) {
            throw new BusinessException("Senha é obrigatória para novos usuários");
        }

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .nome(request.getNome())
                .perfil(Usuario.Perfil.valueOf(perfil))
                .status(request.getStatus() != null ? request.getStatus() : "ATIVO")
                .build();

        return toResponse(repository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: id=" + id));

        if (!usuario.getEmail().equals(request.getEmail())
                && repository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Já existe usuário com o e-mail: " + request.getEmail());
        }

        String perfil = request.getPerfil().toUpperCase();
        if (!PERFIS_VALIDOS.contains(perfil)) {
            throw new BusinessException("Perfil inválido: " + request.getPerfil());
        }

        usuario.setEmail(request.getEmail());
        usuario.setNome(request.getNome());
        usuario.setPerfil(Usuario.Perfil.valueOf(perfil));
        if (request.getStatus() != null) usuario.setStatus(request.getStatus());

        // Só atualiza a senha se foi informada
        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        }

        return toResponse(repository.save(usuario));
    }

    @Transactional
    public void inativar(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: id=" + id));
        usuario.setStatus("INATIVO");
        repository.save(usuario);
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado: id=" + id);
        }
        repository.deleteById(id);
    }

    private UsuarioResponse toResponse(Usuario u) {
        UsuarioResponse r = new UsuarioResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setNome(u.getNome());
        r.setPerfil(u.getPerfil().name());
        r.setStatus(u.getStatus());
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }
}