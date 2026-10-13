package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.request.ChecklistRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.ChecklistResponse;
import br.ce.crateus.fpo.sicraf.domain.service.ChecklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService service;

    /**
     * GET /api/checklists?status=EM_ANDAMENTO&veiculoId=1&motoristaId=2&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<ChecklistResponse>> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long veiculoId,
            @RequestParam(required = false) Long motoristaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listar(status, veiculoId, motoristaId, pageable));
    }

    /**
     * GET /api/checklists/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChecklistResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * GET /api/checklists/veiculo/{veiculoId}
     */
    @GetMapping("/veiculo/{veiculoId}")
    public ResponseEntity<List<ChecklistResponse>> listarPorVeiculo(@PathVariable Long veiculoId) {
        return ResponseEntity.ok(service.listarPorVeiculo(veiculoId));
    }

    /**
     * GET /api/checklists/motorista/{motoristaId}
     */
    @GetMapping("/motorista/{motoristaId}")
    public ResponseEntity<List<ChecklistResponse>> listarPorMotorista(@PathVariable Long motoristaId) {
        return ResponseEntity.ok(service.listarPorMotorista(motoristaId));
    }

    /**
     * POST /api/checklists
     */
    @PostMapping
    public ResponseEntity<ChecklistResponse> criar(@Valid @RequestBody ChecklistRequest request) {
        ChecklistResponse response = service.criar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * PATCH /api/checklists/{id}/concluir
     */
    @PatchMapping("/{id}/concluir")
    public ResponseEntity<ChecklistResponse> concluir(@PathVariable Long id) {
        return ResponseEntity.ok(service.concluir(id));
    }

    /**
     * PATCH /api/checklists/{id}/cancelar
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ChecklistResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    /**
     * DELETE /api/checklists/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}