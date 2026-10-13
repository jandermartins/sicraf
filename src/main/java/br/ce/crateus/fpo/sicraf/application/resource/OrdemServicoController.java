package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.request.OrdemServicoRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.OrdemServicoResponse;
import br.ce.crateus.fpo.sicraf.domain.service.OrdemServicoService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/ordens-servico")
@RequiredArgsConstructor
public class OrdemServicoController {

    private final OrdemServicoService service;

    @GetMapping
    public ResponseEntity<Page<OrdemServicoResponse>> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long veiculoId,
            @RequestParam(required = false) String tipoOficina,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listar(status, veiculoId, tipoOficina, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/numero/{numeroOs}")
    public ResponseEntity<OrdemServicoResponse> buscarPorNumero(@PathVariable String numeroOs) {
        return ResponseEntity.ok(service.buscarPorNumero(numeroOs));
    }

    @GetMapping("/veiculo/{veiculoId}")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorVeiculo(@PathVariable Long veiculoId) {
        return ResponseEntity.ok(service.listarPorVeiculo(veiculoId));
    }

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> criar(@Valid @RequestBody OrdemServicoRequest request) {
        OrdemServicoResponse response = service.criar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * PATCH /api/ordens-servico/{id}/status
     * Body: { "status": "EM_ANDAMENTO" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrdemServicoResponse> atualizarStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String novoStatus = body.get("status");
        return ResponseEntity.ok(service.atualizarStatus(id, novoStatus));
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<OrdemServicoResponse> concluir(@PathVariable Long id) {
        return ResponseEntity.ok(service.concluir(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<OrdemServicoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}