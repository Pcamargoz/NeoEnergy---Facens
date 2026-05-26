package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.model.SoloEntity;
import com.example.NEO_ENERGY.objects.repository.SoloRepository;
import com.example.NEO_ENERGY.objects.repository.spec.SoloSpec;
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
public class SoloService {

    private final SoloRepository repository;

    public SoloEntity salvar(SoloEntity solo) {
        return repository.save(solo);
    }

    public List<SoloEntity> listarTodos() {
        return repository.findAll();
    }

    public Optional<SoloEntity> obterPorId(UUID id) {
        return repository.findById(id);
    }

    public List<SoloEntity> listarPorStatus(boolean statusSolo) {
        return repository.findByStatusSolo(statusSolo);
    }

    public SoloEntity atualizar(SoloEntity solo) {
        repository.findById(solo.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solo não encontrado."));
        return repository.save(solo);
    }

    public SoloEntity atualizarStatus(UUID id, boolean novoStatus) {
        SoloEntity solo = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solo não encontrado."));
        solo.setStatusSolo(novoStatus);
        return repository.save(solo);
    }

    public void deletar(UUID id) {
        SoloEntity solo = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solo não encontrado."));
        repository.delete(solo);
    }

    public List<SoloEntity> pesquisar(Boolean statusSolo, BigDecimal plantacoesMin, BigDecimal plantacoesMax) {
        Specification<SoloEntity> spec = Specification.where((PredicateSpecification<SoloEntity>) null);
        if (statusSolo != null) {
            spec = spec.and(SoloSpec.statusSoloEqual(statusSolo));
        }
        if (plantacoesMin != null) {
            spec = spec.and(SoloSpec.plantacoesMaiorIgual(plantacoesMin));
        }
        if (plantacoesMax != null) {
            spec = spec.and(SoloSpec.plantacoesMenorIgual(plantacoesMax));
        }
        return repository.findAll(spec);
    }
}
