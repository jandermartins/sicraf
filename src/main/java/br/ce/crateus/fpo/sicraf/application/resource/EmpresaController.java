package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.request.EmpresaRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.EmpresaResponse;
import br.ce.crateus.fpo.sicraf.domain.service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresa")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService service;

    /**
     * GET /api/empresa
     * Retorna os dados da empresa (singleton).
     */
    @GetMapping
    public ResponseEntity<EmpresaResponse> obter() {
        return ResponseEntity.ok(service.obter());
    }

    /**
     * PUT /api/empresa
     * Cria ou atualiza os dados da empresa (upsert).
     */
    @PutMapping
    public ResponseEntity<EmpresaResponse> salvar(@Valid @RequestBody EmpresaRequest request) {
        return ResponseEntity.ok(service.salvar(request));
    }

    /**
     * DELETE /api/empresa
     * Remove os dados da empresa (reset).
     */
    @DeleteMapping
    public ResponseEntity<Void> remover() {
        service.remover();
        return ResponseEntity.noContent().build();
    }
}