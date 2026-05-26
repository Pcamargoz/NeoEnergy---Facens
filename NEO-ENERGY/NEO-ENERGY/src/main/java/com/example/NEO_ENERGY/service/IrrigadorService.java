package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.model.IrrigadorEntity;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import com.example.NEO_ENERGY.objects.repository.IrrigadorRepository;
import com.example.NEO_ENERGY.objects.repository.spec.IrrigadorSpec;
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
public class IrrigadorService {

    private final IrrigadorRepository repository;

    public IrrigadorEntity salvar(IrrigadorEntity irrigador) {
        return repository.save(irrigador);
    }

    public List<IrrigadorEntity> listarTodos() {
        return repository.findAll();
    }

    public Optional<IrrigadorEntity> obterPorId(UUID id) {
        return repository.findById(id);
    }

    public List<IrrigadorEntity> listarPorStatus(STATUS_OBJETOS status) {
        return repository.findByStatus(status);
    }

    public IrrigadorEntity atualizar(IrrigadorEntity irrigador) {
        repository.findById(irrigador.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Irrigador não encontrado."));
        return repository.save(irrigador);
    }

    public IrrigadorEntity atualizarStatus(UUID id, STATUS_OBJETOS novoStatus) {
        IrrigadorEntity irrigador = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Irrigador não encontrado."));
        irrigador.setStatus(novoStatus);
        return repository.save(irrigador);
    }

    public void deletar(UUID id) {
        IrrigadorEntity irrigador = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Irrigador não encontrado."));
        repository.delete(irrigador);
    }

    public List<IrrigadorEntity> pesquisar(STATUS_OBJETOS status, BigDecimal aguaMin, BigDecimal aguaMax) {
        Specification<IrrigadorEntity> spec = Specification.where((PredicateSpecification<IrrigadorEntity>) null);
        if (status != null) {
            spec = spec.and(IrrigadorSpec.statusEqual(status));
        }
        if (aguaMin != null) {
            spec = spec.and(IrrigadorSpec.aguaMaiorIgual(aguaMin));
        }
        if (aguaMax != null) {
            spec = spec.and(IrrigadorSpec.aguaMenorIgual(aguaMax));
        }
        return repository.findAll(spec);
    }
}
