package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.response.LogAuditoriaResponse;
import br.ce.crateus.fpo.sicraf.domain.model.LogAuditoria;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.LogAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditoriaService {

    private final LogAuditoriaRepository repository;

    public Page<LogAuditoriaResponse> listar(String acao, String entidade,
                                             String usuarioEmail,
                                             LocalDateTime dataInicio,
                                             LocalDateTime dataFim,
                                             Pageable pageable) {
        return repository.findByFilters(acao, entidade, usuarioEmail,
                dataInicio, dataFim, pageable).map(this::toResponse);
    }

    /**
     * Registra uma ação no log de auditoria.
     * Chamado pelos services quando uma operação relevante ocorre.
     */
    @Transactional
    public void registrar(String usuarioEmail, String acao, String entidade, Long entidadeId, String detalhes) {
        try {
            LogAuditoria log = LogAuditoria.builder()
                    .usuarioEmail(usuarioEmail)
                    .acao(acao)
                    .entidade(entidade)
                    .entidadeId(entidadeId)
                    .detalhes(detalhes)
                    .build();
            repository.save(log);
        } catch (Exception e) {
            // Auditoria não deve quebrar a operação principal
            log.warn("Falha ao registrar log de auditoria", e);
        }
    }

    private LogAuditoriaResponse toResponse(LogAuditoria l) {
        LogAuditoriaResponse r = new LogAuditoriaResponse();
        r.setId(l.getId());
        r.setUsuarioEmail(l.getUsuarioEmail());
        r.setAcao(l.getAcao());
        r.setEntidade(l.getEntidade());
        r.setEntidadeId(l.getEntidadeId());
        r.setDetalhes(l.getDetalhes());
        r.setIpAddress(l.getIpAddress());
        r.setCreatedAt(l.getCreatedAt());
        return r;
    }
}