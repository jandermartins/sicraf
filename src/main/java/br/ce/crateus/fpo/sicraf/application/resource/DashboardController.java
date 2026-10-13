package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.response.DashboardResponse;
import br.ce.crateus.fpo.sicraf.domain.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    /**
     * GET /api/dashboard
     * GET /api/dashboard?dataInicio=2026-01-01&dataFim=2026-12-31
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> obterDashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        return ResponseEntity.ok(service.obterDashboard(dataInicio, dataFim));
    }
}