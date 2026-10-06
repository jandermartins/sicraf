package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.request.MotoristaRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.MotoristaResponse;
import br.ce.crateus.fpo.sicraf.domain.service.MotoristaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/motoristas")
@RequiredArgsConstructor
public class MotoristaController {

    private final MotoristaService service;

    /**
     * GET /api/motoristas?status=ATIVO&secretaria=Saúde&nome=joao&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<MotoristaResponse>> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String secretaria,
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        return ResponseEntity.ok(service.listar(status, secretaria, nome, pageable));
    }

    /**
     * GET /api/motoristas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MotoristaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * GET /api/motoristas/cpf/{cpf}
     */
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<MotoristaResponse> buscarPorCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(service.buscarPorCpf(cpf));
    }

    /**
     * POST /api/motoristas
     */
    @PostMapping
    public ResponseEntity<MotoristaResponse> criar(@Valid @RequestBody MotoristaRequest request) {
        MotoristaResponse response = service.criar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * PUT /api/motoristas/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<MotoristaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MotoristaRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    /**
     * PATCH /api/motoristas/{id}/inativar
     */
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/motoristas/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // RF010 - Alertas de CNH (Dashboard)
    // ============================================

    /**
     * GET /api/motoristas/alertas/cnh-vencendo?dias=30
     * Lista motoristas com CNH vencendo nos próximos N dias.
     */
    @GetMapping("/alertas/cnh-vencendo")
    public ResponseEntity<List<MotoristaResponse>> listarCnhVencendo(
            @RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(service.listarCnhVencendo(dias));
    }

    /**
     * GET /api/motoristas/alertas/cnh-vencida
     * Lista motoristas com CNH já vencida.
     */
    @GetMapping("/alertas/cnh-vencida")
    public ResponseEntity<List<MotoristaResponse>> listarCnhVencida() {
        return ResponseEntity.ok(service.listarCnhVencida());
    }
}
