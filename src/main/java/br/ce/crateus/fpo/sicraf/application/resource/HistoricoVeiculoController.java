package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.response.HistoricoVeiculoResponse;
import br.ce.crateus.fpo.sicraf.domain.service.HistoricoVeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/historico")
@RequiredArgsConstructor
public class HistoricoVeiculoController {

    private final HistoricoVeiculoService service;

    /**
     * GET /api/historico/veiculo/{veiculoId}
     * GET /api/historico/veiculo/{veiculoId}?dataInicio=2026-01-01&dataFim=2026-12-31
     * GET /api/historico/veiculo/{veiculoId}?tipoEvento=ABASTECIMENTO
     */
    @GetMapping("/veiculo/{veiculoId}")
    public ResponseEntity<HistoricoVeiculoResponse> obterHistorico(
            @PathVariable Long veiculoId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String tipoEvento) {

        return ResponseEntity.ok(service.obterHistorico(veiculoId, dataInicio, dataFim, tipoEvento));
    }
}