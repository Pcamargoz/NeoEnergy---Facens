package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.dto.SoloDTO;
import com.example.NEO_ENERGY.objects.model.CasaEntity;
import com.example.NEO_ENERGY.objects.model.SoloEntity;
import com.example.NEO_ENERGY.objects.model.TiposDoSolo;
import com.example.NEO_ENERGY.objects.repository.CasaRepository;
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
    private final CasaRepository casaRepository;

    public SoloEntity salvar(SoloEntity solo) {
        return repository.save(solo);
    }

    public SoloEntity criarDeDTO(SoloDTO dto) {
        CasaEntity casa = casaRepository.findById(dto.casaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Casa não encontrada: " + dto.casaId()));
        SoloEntity solo = new SoloEntity();
        solo.setNomeDoSolo(dto.nomeDoSolo());
        solo.setStatusSolo(dto.statusSolo());
        solo.setTiposDoSolo(dto.tiposDoSolo());
        solo.setCasa(casa);
        return repository.save(solo);
    }

    public SoloEntity atualizarDeDTO(UUID id, SoloDTO dto) {
        SoloEntity existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solo não encontrado."));
        CasaEntity casa = casaRepository.findById(dto.casaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Casa não encontrada: " + dto.casaId()));
        existente.setNomeDoSolo(dto.nomeDoSolo());
        existente.setStatusSolo(dto.statusSolo());
        existente.setTiposDoSolo(dto.tiposDoSolo());
        existente.setCasa(casa);
        return repository.save(existente);
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

    // verificar esse metodo depois tambem
    public SoloEntity verificarChuva(SoloEntity solo, boolean molhadoOuNao){
        if(molhadoOuNao != false){
           Optional<SoloEntity>SoloEncontrado = repository.findById(solo.getId());
           SoloEncontrado.get().setTiposDoSolo(TiposDoSolo.MOLHADO);
        }
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

    public List<SoloEntity> pesquisar(Boolean statusSolo) {
        Specification<SoloEntity> spec = Specification.where((PredicateSpecification<SoloEntity>) null);
        if (statusSolo != null) {
            spec = spec.and(SoloSpec.statusSoloEqual(statusSolo));
        }
        return repository.findAll(spec);
    }
}
