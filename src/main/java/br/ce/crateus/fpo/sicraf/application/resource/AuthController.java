package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.request.LoginRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.TokenResponse;
import br.ce.crateus.fpo.sicraf.domain.model.Usuario;
import br.ce.crateus.fpo.sicraf.domain.service.JwtService;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String accessToken = jwtService.generateAccessToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        // Refresh token em cookie HttpOnly
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true em produção (HTTPS)
        cookie.setPath("/api/auth");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 dias
        response.addCookie(cookie);

        return ResponseEntity.ok(new TokenResponse(
                accessToken,
                "Bearer",
                jwtService.extractPerfil(accessToken)
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null || !jwtService.isTokenValid(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        String email = jwtService.extractUsername(refreshToken);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String newAccessToken = jwtService.generateAccessToken(usuario);

        return ResponseEntity.ok(new TokenResponse(
                newAccessToken, "Bearer", usuario.getPerfil().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/api/auth");
        response.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }

    // Seed de usuário admin (temporário para desenvolvimento)
    @PostMapping("/seed")
    public ResponseEntity<Usuario> seed() {
        if (usuarioRepository.existsByEmail("admin@sicraf.local")) {
            return ResponseEntity.ok(usuarioRepository.findByEmail("admin@sicraf.local").get());
        }
        Usuario admin = Usuario.builder()
                .email("admin@sicraf.local")
                .senha(passwordEncoder.encode("admin123"))
                .nome("Administrador")
                .perfil(Usuario.Perfil.GESTOR)
                .build();
        return ResponseEntity.ok(usuarioRepository.save(admin));
    }
}
