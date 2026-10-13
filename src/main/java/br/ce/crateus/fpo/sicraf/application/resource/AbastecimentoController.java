package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.request.AbastecimentoRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.AbastecimentoResponse;
import br.ce.crateus.fpo.sicraf.domain.service.AbastecimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/abastecimentos")
@RequiredArgsConstructor
public class AbastecimentoController {

    private final AbastecimentoService service;

    /**
     * GET /api/abastecimentos?veiculoId=1&motoristaId=2&dataInicio=2026-01-01&dataFim=2026-12-31&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<AbastecimentoResponse>> listar(
            @RequestParam(required = false) Long veiculoId,
            @RequestParam(required = false) Long motoristaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listar(veiculoId, motoristaId, dataInicio, dataFim, pageable));
    }

    /**
     * GET /api/abastecimentos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AbastecimentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * GET /api/abastecimentos/veiculo/{veiculoId}
     */
    @GetMapping("/veiculo/{veiculoId}")
    public ResponseEntity<List<AbastecimentoResponse>> listarPorVeiculo(@PathVariable Long veiculoId) {
        return ResponseEntity.ok(service.listarPorVeiculo(veiculoId));
    }

    /**
     * GET /api/abastecimentos/motorista/{motoristaId}
     */
    @GetMapping("/motorista/{motoristaId}")
    public ResponseEntity<List<AbastecimentoResponse>> listarPorMotorista(@PathVariable Long motoristaId) {
        return ResponseEntity.ok(service.listarPorMotorista(motoristaId));
    }

    /**
     * POST /api/abastecimentos
     */
    @PostMapping
    public ResponseEntity<AbastecimentoResponse> criar(@Valid @RequestBody AbastecimentoRequest request) {
        AbastecimentoResponse response = service.criar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * PUT /api/abastecimentos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AbastecimentoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AbastecimentoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    /**
     * DELETE /api/abastecimentos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}