package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.response.CustoKmResponse;
import br.ce.crateus.fpo.sicraf.domain.service.RelatorioCustoKmService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/relatorios/custo-km")
@RequiredArgsConstructor
public class RelatorioCustoKmController {

    private final RelatorioCustoKmService service;

    /**
     * GET /api/relatorios/custo-km?dataInicio=2026-01-01&dataFim=2026-12-31
     * GET /api/relatorios/custo-km?dataInicio=2026-01-01&dataFim=2026-12-31&veiculoId=1
     */
    @GetMapping
    public ResponseEntity<CustoKmResponse> gerar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Long veiculoId) {

        return ResponseEntity.ok(service.gerar(dataInicio, dataFim, veiculoId));
    }
}