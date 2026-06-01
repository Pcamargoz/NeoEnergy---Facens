package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.dto.CasaDTO;
import com.example.NEO_ENERGY.objects.model.CasaEntity;
import com.example.NEO_ENERGY.objects.repository.CasaRepository;
import com.example.NEO_ENERGY.objects.repository.spec.CasaSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CasaService {

    private final CasaRepository repository;

    public CasaEntity salvar(CasaEntity casa) {
        return repository.save(casa);
    }

    // Cria uma casa vazia (CasaDTO não tem campos escalares próprios por enquanto).
    public CasaEntity criarDeDTO(CasaDTO dto) {
        return repository.save(new CasaEntity());
    }

    // Atualizar uma casa sem campos próprios é praticamente um no-op; só valida existência.
    public CasaEntity atualizarDeDTO(UUID id, CasaDTO dto) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Casa não encontrada."));
    }

    public List<CasaEntity> listarTodos() {
        return repository.findAll();
    }

    public Optional<CasaEntity> obterPorId(UUID id) {
        return repository.findById(id);
    }

    public CasaEntity atualizar(CasaEntity casa) {
        repository.findById(casa.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Casa não encontrada."));
        return repository.save(casa);
    }

    public void deletar(UUID id) {
        CasaEntity casa = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Casa não encontrada."));
        repository.delete(casa);
    }

    public List<CasaEntity> pesquisar(UUID idPainelSolar, UUID idSolo) {
        Specification<CasaEntity> spec = Specification.where((PredicateSpecification<CasaEntity>) null);
        if (idPainelSolar != null) {
            spec = spec.and(CasaSpec.painelSolarIdEqual(idPainelSolar));
        }
        if (idSolo != null) {
            spec = spec.and(CasaSpec.soloIdEqual(idSolo));
        }
        return repository.findAll(spec);
    }
}
