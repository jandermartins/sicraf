package br.ce.crateus.fpo.sicraf.application.resource;

import br.ce.crateus.fpo.sicraf.domain.dto.response.AlertaManutencaoResponse;
import br.ce.crateus.fpo.sicraf.domain.service.AlertaManutencaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas-manutencao")
@RequiredArgsConstructor
public class AlertaManutencaoController {

    private final AlertaManutencaoService service;

    @GetMapping
    public ResponseEntity<Page<AlertaManutencaoResponse>> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long veiculoId,
            @RequestParam(required = false) Long tipoManutencaoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listar(status, veiculoId, tipoManutencaoId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaManutencaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * Lista todos os alertas pendentes (usado no dashboard).
     */
    @GetMapping("/pendentes")
    public ResponseEntity<List<AlertaManutencaoResponse>> listarPendentes() {
        return ResponseEntity.ok(service.listarPendentes());
    }

    /**
     * Força uma verificação manual (útil para testes).
     */
    @PostMapping("/verificar")
    public ResponseEntity<Map<String, Object>> verificarAgora() {
        int gerados = service.verificarEGerarAlertas();
        return ResponseEntity.ok(Map.of(
                "alertasGerados", gerados,
                "mensagem", gerados > 0
                        ? gerados + " alertas gerados com sucesso"
                        : "Nenhum alerta novo foi necessário"
        ));
    }

    @PatchMapping("/{id}/agendar")
    public ResponseEntity<AlertaManutencaoResponse> agendar(@PathVariable Long id) {
        return ResponseEntity.ok(service.agendar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AlertaManutencaoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}