package br.ce.crateus.fpo.sicraf.application.scheduler;

import br.ce.crateus.fpo.sicraf.domain.service.AlertaManutencaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AlertaManutencaoScheduler {

    private final AlertaManutencaoService service;

    /**
     * Executa todos os dias às 06:00 da manhã.
     * Cron: segundo minuto hora dia mês dia-semana
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void verificarAlertasDiariamente() {
        log.info("SICRAF: Iniciando verificação diária de alertas de manutenção...");
        try {
            int gerados = service.verificarEGerarAlertas();
            log.info("SICRAF: Verificação concluída. {} alertas gerados.", gerados);
        } catch (Exception e) {
            log.error("SICRAF: Erro na verificação de alertas", e);
        }
    }

    /**
     * Executa a cada 1 hora (opcional, para testes).
     * Descomente se quiser verificar com mais frequência.
     */
    // @Scheduled(fixedRate = 3600000) // 1 hora em ms
    // public void verificarAlertasPorHora() {
    //     service.verificarEGerarAlertas();
    // }
}