package br.ce.crateus.fpo.sicraf.application.config;

import br.ce.crateus.fpo.sicraf.application.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(context -> context
                        .requireExplicitSave(false))  // ← IMPORTANTE
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/veiculos/**").hasAnyRole("GESTOR", "COORDENADOR")
                        .requestMatchers("/api/motoristas/**").hasAnyRole("GESTOR", "COORDENADOR")
                        .requestMatchers("/api/tipos-manutencao/**").hasAnyRole("GESTOR", "MECANICO")
                        .requestMatchers("/api/checklists/**").hasAnyRole("GESTOR", "COORDENADOR", "MOTORISTA")
                        .requestMatchers("/api/abastecimentos/**").hasAnyRole("GESTOR", "COORDENADOR", "MOTORISTA")
                        .requestMatchers("/api/ocorrencias/**").hasAnyRole("GESTOR", "COORDENADOR", "MOTORISTA")
                        .requestMatchers("/api/ordens-servico/**").hasAnyRole("GESTOR", "MECANICO")
                        .requestMatchers("/api/alertas-manutencao/**").hasAnyRole("GESTOR", "COORDENADOR", "MECANICO")
                        .requestMatchers("/api/historico/**").hasAnyRole("GESTOR", "COORDENADOR")
                        .requestMatchers("/api/dashboard/**").hasAnyRole("GESTOR", "COORDENADOR")
                        .requestMatchers("/api/relatorios/**").hasAnyRole("GESTOR", "COORDENADOR")
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "http://localhost:4200", "http://localhost:8443", "http://localhost:8080"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}