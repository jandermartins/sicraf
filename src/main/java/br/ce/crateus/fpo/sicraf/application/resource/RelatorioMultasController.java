package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.response.MultasMotoristaResponse;
import br.ce.crateus.fpo.sicraf.domain.service.RelatorioMultasService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/relatorios/multas-motorista")
@RequiredArgsConstructor
public class RelatorioMultasController {

    private final RelatorioMultasService service;

    /**
     * GET /api/relatorios/multas-motorista
     * GET /api/relatorios/multas-motorista?dataInicio=2026-01-01&dataFim=2026-12-31
     * GET /api/relatorios/multas-motorista?status=PENDENTE
     */
    @GetMapping
    public ResponseEntity<MultasMotoristaResponse> gerar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(service.gerar(dataInicio, dataFim, status));
    }
}