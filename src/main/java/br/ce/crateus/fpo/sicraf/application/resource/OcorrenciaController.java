package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.request.OcorrenciaRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.OcorrenciaResponse;
import br.ce.crateus.fpo.sicraf.domain.service.OcorrenciaService;
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
@RequestMapping("/api/ocorrencias")
@RequiredArgsConstructor
public class OcorrenciaController {

    private final OcorrenciaService service;

    /**
     * GET /api/ocorrencias?tipo=MULTA&status=PENDENTE&veiculoId=1&dataInicio=2026-01-01
     */
    @GetMapping
    public ResponseEntity<Page<OcorrenciaResponse>> listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long veiculoId,
            @RequestParam(required = false) Long motoristaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listar(tipo, status, veiculoId, motoristaId,
                dataInicio, dataFim, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OcorrenciaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/veiculo/{veiculoId}")
    public ResponseEntity<List<OcorrenciaResponse>> listarPorVeiculo(@PathVariable Long veiculoId) {
        return ResponseEntity.ok(service.listarPorVeiculo(veiculoId));
    }

    @GetMapping("/motorista/{motoristaId}")
    public ResponseEntity<List<OcorrenciaResponse>> listarPorMotorista(@PathVariable Long motoristaId) {
        return ResponseEntity.ok(service.listarPorMotorista(motoristaId));
    }

    @PostMapping
    public ResponseEntity<OcorrenciaResponse> criar(@Valid @RequestBody OcorrenciaRequest request) {
        OcorrenciaResponse response = service.criar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OcorrenciaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody OcorrenciaRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    /**
     * PATCH /api/ocorrencias/{id}/pagar
     */
    @PatchMapping("/{id}/pagar")
    public ResponseEntity<OcorrenciaResponse> pagar(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarComoPaga(id));
    }

    /**
     * PATCH /api/ocorrencias/{id}/recorrer
     */
    @PatchMapping("/{id}/recorrer")
    public ResponseEntity<OcorrenciaResponse> recorrer(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarComoRecorrida(id));
    }

    /**
     * PATCH /api/ocorrencias/{id}/cancelar
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<OcorrenciaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}