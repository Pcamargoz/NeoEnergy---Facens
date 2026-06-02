package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.dto.PsolarDTO;
import com.example.NEO_ENERGY.objects.model.CasaEntity;
import com.example.NEO_ENERGY.objects.model.PsolarEntity;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import com.example.NEO_ENERGY.objects.repository.CasaRepository;
import com.example.NEO_ENERGY.objects.repository.PsolarRepository;
import com.example.NEO_ENERGY.objects.repository.spec.PsolarSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PsolarService {

    private final PsolarRepository repository;
    private final CasaRepository casaRepository;

    public PsolarEntity salvar(PsolarEntity psolar) {
        return repository.save(psolar);
    }

    // Cria a partir do DTO resolvendo a FK casa via casaRepository.
    public PsolarEntity criarDeDTO(PsolarDTO dto) {
        CasaEntity casa = casaRepository.findById(dto.casaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Casa não encontrada: " + dto.casaId()));
        PsolarEntity psolar = new PsolarEntity();
        psolar.setNome(dto.nome());
        psolar.setEnergiaPsolar(dto.energia());
        psolar.setStatus(dto.status());
        psolar.setCasa(casa);
        return repository.save(psolar);
    }

    public PsolarEntity atualizarDeDTO(UUID id, PsolarDTO dto) {
        PsolarEntity existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Painel solar não encontrado."));
        CasaEntity casa = casaRepository.findById(dto.casaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Casa não encontrada: " + dto.casaId()));
        existente.setNome(dto.nome());
        existente.setEnergiaPsolar(dto.energia());
        existente.setStatus(dto.status());
        existente.setCasa(casa);
        return repository.save(existente);
    }

    public List<PsolarEntity> listarTodos() {
        return repository.findAll();
    }

    public Optional<PsolarEntity> obterPorId(UUID id) {
        return repository.findById(id);
    }

    public List<PsolarEntity> listarPorStatus(STATUS_OBJETOS status) {
        return repository.findByStatus(status);
    }

    public PsolarEntity atualizar(PsolarEntity psolar) {
        repository.findById(psolar.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Painel solar não encontrado."));
        return repository.save(psolar);
    }

    // Atualiza SOMENTE a energia do painel: carrega o existente e copia a nova energia nele.
    // (Antes estava ao contrário — sobrescrevia a energia nova com a antiga.)
    public PsolarEntity atualizarEnergia(PsolarEntity psolar){
        PsolarEntity existente = repository.findById(psolar.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Painel solar não encontrado."));
        existente.setEnergiaPsolar(psolar.getEnergiaPsolar());
        return repository.save(existente);
    }

    public PsolarEntity atualizarStatus(UUID id, STATUS_OBJETOS novoStatus) {
        PsolarEntity psolar = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Painel solar não encontrado."));
        psolar.setStatus(novoStatus);
        return repository.save(psolar);
    }

    public void deletar(UUID id) {
        PsolarEntity psolar = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Painel solar não encontrado."));
        repository.delete(psolar);
    }

    public List<PsolarEntity> pesquisar(STATUS_OBJETOS status, BigDecimal energiaMin, BigDecimal energiaMax) {
        Specification<PsolarEntity> spec = Specification.where((PredicateSpecification<PsolarEntity>) null);
        if (status != null) {
            spec = spec.and(PsolarSpec.statusEqual(status));
        }
        if (energiaMin != null) {
            spec = spec.and(PsolarSpec.energiaMaiorIgual(energiaMin));
        }
        if (energiaMax != null) {
            spec = spec.and(PsolarSpec.energiaMenorIgual(energiaMax));
        }
        return repository.findAll(spec);
    }
}
