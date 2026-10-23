package br.ce.crateus.fpo.sicraf.domain.service;

import br.ce.crateus.fpo.sicraf.domain.dto.request.EmpresaRequest;
import br.ce.crateus.fpo.sicraf.domain.dto.response.EmpresaResponse;
import br.ce.crateus.fpo.sicraf.domain.exception.BusinessException;
import br.ce.crateus.fpo.sicraf.domain.exception.ResourceNotFoundException;
import br.ce.crateus.fpo.sicraf.domain.model.Empresa;
import br.ce.crateus.fpo.sicraf.infrastructure.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository repository;

    /**
     * Retorna os dados da empresa (singleton).
     * Se não existir, retorna 404 para o frontend saber que precisa cadastrar.
     */
    public EmpresaResponse obter() {
        List<Empresa> todas = repository.findAll();
        if (todas.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Dados da empresa ainda não foram cadastrados. Use PUT /api/empresa para cadastrar.");
        }
        return toResponse(todas.get(0));
    }

    /**
     * Cria ou atualiza os dados da empresa (upsert).
     * Como é singleton, sempre opera sobre o primeiro registro.
     */
    @Transactional
    public EmpresaResponse salvar(EmpresaRequest request) {
        String cnpjLimpo = limparDocumento(request.getCnpj());
        request.setCnpj(cnpjLimpo);

        List<Empresa> todas = repository.findAll();
        Empresa empresa;

        if (todas.isEmpty()) {
            // Criar novo
            empresa = Empresa.builder()
                    .razaoSocial(request.getRazaoSocial())
                    .nomeFantasia(request.getNomeFantasia())
                    .cnpj(cnpjLimpo)
                    .endereco(request.getEndereco())
                    .cidade(request.getCidade())
                    .uf(request.getUf())
                    .cep(limparDocumento(request.getCep()))
                    .telefone(request.getTelefone())
                    .email(request.getEmail())
                    .site(request.getSite())
                    .logoUrl(request.getLogoUrl())
                    .responsavelLegal(request.getResponsavelLegal())
                    .build();
        } else {
            // Atualizar existente
            empresa = todas.get(0);

            // Validar CNPJ duplicado (se mudou)
            if (!empresa.getCnpj().equals(cnpjLimpo) && repository.existsByCnpj(cnpjLimpo)) {
                throw new BusinessException("Já existe empresa cadastrada com o CNPJ " + cnpjLimpo);
            }

            empresa.setRazaoSocial(request.getRazaoSocial());
            empresa.setNomeFantasia(request.getNomeFantasia());
            empresa.setCnpj(cnpjLimpo);
            empresa.setEndereco(request.getEndereco());
            empresa.setCidade(request.getCidade());
            empresa.setUf(request.getUf());
            empresa.setCep(limparDocumento(request.getCep()));
            empresa.setTelefone(request.getTelefone());
            empresa.setEmail(request.getEmail());
            empresa.setSite(request.getSite());
            empresa.setLogoUrl(request.getLogoUrl());
            empresa.setResponsavelLegal(request.getResponsavelLegal());
        }

        return toResponse(repository.save(empresa));
    }

    /**
     * Remove os dados da empresa (usado em testes/reset).
     */
    @Transactional
    public void remover() {
        repository.deleteAll();
    }

    // ---------- Helpers ----------

    private String limparDocumento(String doc) {
        return doc == null ? null : doc.replaceAll("\\D", "");
    }

    private EmpresaResponse toResponse(Empresa e) {
        EmpresaResponse r = new EmpresaResponse();
        r.setId(e.getId());
        r.setRazaoSocial(e.getRazaoSocial());
        r.setNomeFantasia(e.getNomeFantasia());
        r.setCnpj(e.getCnpj());
        r.setEndereco(e.getEndereco());
        r.setCidade(e.getCidade());
        r.setUf(e.getUf());
        r.setCep(e.getCep());
        r.setTelefone(e.getTelefone());
        r.setEmail(e.getEmail());
        r.setSite(e.getSite());
        r.setLogoUrl(e.getLogoUrl());
        r.setResponsavelLegal(e.getResponsavelLegal());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}