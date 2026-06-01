package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.dto.IrrigadorDTO;
import com.example.NEO_ENERGY.objects.model.IrrigadorEntity;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import com.example.NEO_ENERGY.objects.model.SessaoIrrigador;
import com.example.NEO_ENERGY.objects.model.SoloEntity;
import com.example.NEO_ENERGY.objects.repository.IrrigadorRepository;
import com.example.NEO_ENERGY.objects.repository.SoloRepository;
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
    private final SessaoIrrigadorService sessaoService;
    private final SoloRepository soloRepository;

    public IrrigadorEntity salvar(IrrigadorEntity irrigador) {
        return repository.save(irrigador);
    }

    public IrrigadorEntity criarDeDTO(IrrigadorDTO dto) {
        SoloEntity solo = soloRepository.findById(dto.soloId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solo não encontrado: " + dto.soloId()));
        IrrigadorEntity irrigador = new IrrigadorEntity();
        irrigador.setNome(dto.nome());
        irrigador.setStatus(dto.status());
        irrigador.setSolo(solo);
        return repository.save(irrigador);
    }

    public IrrigadorEntity atualizarDeDTO(UUID id, IrrigadorDTO dto) {
        IrrigadorEntity existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Irrigador não encontrado."));
        SoloEntity solo = soloRepository.findById(dto.soloId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solo não encontrado: " + dto.soloId()));
        existente.setNome(dto.nome());
        existente.setStatus(dto.status());
        existente.setSolo(solo);
        return repository.save(existente);
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

    // climaApto vem do front (consulta de tempo/sensor). Se false, a sessão é recusada lá no SessaoService.
    public IrrigadorEntity ligarRegador(UUID id, boolean climaApto) {
        IrrigadorEntity irrigador = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Irrigador não encontrado."));

        if (irrigador.getStatus() == STATUS_OBJETOS.LIGADO) {
            throw new IllegalStateException("Irrigador já está ligado.");
        }

        // Delega a criação da sessão (e a validação do clima) para o service de sessão.
        sessaoService.abrirSessao(irrigador, climaApto);
        irrigador.setStatus(STATUS_OBJETOS.LIGADO);

        return repository.save(irrigador);
    }

    public IrrigadorEntity desligarRegador(UUID id) {
        IrrigadorEntity irrigador = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Irrigador não encontrado."));

        if (irrigador.getStatus() != STATUS_OBJETOS.LIGADO) {
            throw new IllegalStateException("Irrigador já está desligado.");
        }

        // O service de sessão calcula duração e água deste ciclo e devolve a sessão fechada.
        SessaoIrrigador sessaoFechada = sessaoService.fecharSessaoAberta(irrigador);

        // Acumula no irrigador os totais somando o que veio da sessão.
        long totalAnterior = irrigador.getTempoTotalLigadoSegundos() == null
                ? 0L
                : irrigador.getTempoTotalLigadoSegundos();
        irrigador.setTempoTotalLigadoSegundos(totalAnterior + sessaoFechada.getDuracaoSegundos());

        BigDecimal aguaAnterior = irrigador.getAgua() == null ? BigDecimal.ZERO : irrigador.getAgua();
        irrigador.setAgua(aguaAnterior.add(sessaoFechada.getAgua()));

        irrigador.setStatus(STATUS_OBJETOS.DESLIGADO);

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
