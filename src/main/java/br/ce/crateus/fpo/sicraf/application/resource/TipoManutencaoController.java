package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.request.TipoManutencaoRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.TipoManutencaoResponse;
import br.ce.crateus.fpo.sicraf.domain.service.TipoManutencaoService;
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
@RequestMapping("/api/tipos-manutencao")
@RequiredArgsConstructor
public class TipoManutencaoController {

    private final TipoManutencaoService service;

    /**
     * GET /api/tipos-manutencao?status=ATIVO&tipo=PREVENTIVA&nome=óleo&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<TipoManutencaoResponse>> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        return ResponseEntity.ok(service.listar(status, tipo, nome, pageable));
    }

    /**
     * GET /api/tipos-manutencao/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TipoManutencaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * GET /api/tipos-manutencao/ativos-preventivos
     * Usado pelo módulo de Ordens de Serviço (RF007).
     */
    @GetMapping("/ativos-preventivos")
    public ResponseEntity<List<TipoManutencaoResponse>> listarAtivosPreventivos() {
        return ResponseEntity.ok(service.listarAtivosPreventivos());
    }

    /**
     * POST /api/tipos-manutencao
     */
    @PostMapping
    public ResponseEntity<TipoManutencaoResponse> criar(@Valid @RequestBody TipoManutencaoRequest request) {
        TipoManutencaoResponse response = service.criar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * PUT /api/tipos-manutencao/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TipoManutencaoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoManutencaoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    /**
     * PATCH /api/tipos-manutencao/{id}/inativar
     */
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/tipos-manutencao/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
