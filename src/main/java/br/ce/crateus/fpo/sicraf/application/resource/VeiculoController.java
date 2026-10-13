package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.request.VeiculoRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.VeiculoResponse;
import br.ce.crateus.fpo.sicraf.domain.service.VeiculoService;
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

@RestController
@RequestMapping("/api/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService service;

    /**
     * GET /api/veiculos?status=ATIVO&marca=Toyota&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<VeiculoResponse>> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String marca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "placa") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        return ResponseEntity.ok(service.listar(status, marca, pageable));
    }

    /**
     * GET /api/veiculos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * GET /api/veiculos/placa/{placa}
     */
    @GetMapping("/placa/{placa}")
    public ResponseEntity<VeiculoResponse> buscarPorPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(service.buscarPorPlaca(placa));
    }

    /**
     * POST /api/veiculos
     */
    @PostMapping
    public ResponseEntity<VeiculoResponse> criar(@Valid @RequestBody VeiculoRequest request) {
        VeiculoResponse response = service.criar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * PUT /api/veiculos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody VeiculoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    /**
     * PATCH /api/veiculos/{id}/inativar
     */
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/veiculos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
