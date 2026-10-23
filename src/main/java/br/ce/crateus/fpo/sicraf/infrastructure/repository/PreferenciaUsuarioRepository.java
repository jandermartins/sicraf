package br.ce.crateus.fpo.sicraf.infrastructure.repository;

import br.ce.crateus.fpo.sicraf.domain.model.PreferenciaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenciaUsuarioRepository extends JpaRepository<PreferenciaUsuario, Long> {
    Optional<PreferenciaUsuario> findByUsuarioId(Long usuarioId);
}