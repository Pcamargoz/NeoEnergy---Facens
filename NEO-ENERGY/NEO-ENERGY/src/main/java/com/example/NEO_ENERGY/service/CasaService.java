package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.dto.CasaDTO;
import com.example.NEO_ENERGY.objects.dto.DispositivoDTO;
import com.example.NEO_ENERGY.objects.model.CasaEntity;
import com.example.NEO_ENERGY.objects.model.TipoDispositivo;
import com.example.NEO_ENERGY.objects.repository.CasaRepository;
import com.example.NEO_ENERGY.objects.repository.IrrigadorRepository;
import com.example.NEO_ENERGY.objects.repository.PsolarRepository;
import com.example.NEO_ENERGY.objects.repository.SoloRepository;
import com.example.NEO_ENERGY.objects.repository.spec.CasaSpec;
import com.example.NEO_ENERGY.objects.repository.spec.IrrigadorSpec;
import com.example.NEO_ENERGY.objects.repository.spec.PsolarSpec;
import com.example.NEO_ENERGY.objects.repository.spec.SoloSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CasaService {

    private final CasaRepository repository;
    private final IrrigadorRepository irrigadorRepository;
    private final PsolarRepository psolarRepository;
    private final SoloRepository soloRepository;

    public CasaEntity salvar(CasaEntity casa) {
        return repository.save(casa);
    }

    // Lista os dispositivos de uma casa (irrigadores, painéis e plantações) em forma resumida.
    // Com 'tipo' definido, pagina direto no banco daquele tipo. Sem 'tipo', junta os três e
    // pagina em memória (ok pra poucos dispositivos por casa).
    public Page<DispositivoDTO> listarDispositivos(UUID casaId, TipoDispositivo tipo, Pageable pageable) {
        // Garante que a casa existe antes de listar (404 se não).
        repository.findById(casaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Casa não encontrada."));

        if (tipo == TipoDispositivo.IRRIGADOR) {
            return irrigadorRepository.findAll(IrrigadorSpec.casaIdEqual(casaId), pageable)
                    .map(DispositivoDTO::deIrrigador);
        }
        if (tipo == TipoDispositivo.PAINEL_SOLAR) {
            return psolarRepository.findAll(PsolarSpec.casaIdEqual(casaId), pageable)
                    .map(DispositivoDTO::dePainel);
        }
        if (tipo == TipoDispositivo.PLANTACAO) {
            return soloRepository.findAll(SoloSpec.casaIdEqual(casaId), pageable)
                    .map(DispositivoDTO::dePlantacao);
        }

        // Sem tipo: junta os três tipos numa lista única, ordena por nome e pagina em memória.
        List<DispositivoDTO> todos = new ArrayList<>();
        irrigadorRepository.findAll(IrrigadorSpec.casaIdEqual(casaId))
                .forEach(e -> todos.add(DispositivoDTO.deIrrigador(e)));
        psolarRepository.findAll(PsolarSpec.casaIdEqual(casaId))
                .forEach(e -> todos.add(DispositivoDTO.dePainel(e)));
        soloRepository.findAll(SoloSpec.casaIdEqual(casaId))
                .forEach(e -> todos.add(DispositivoDTO.dePlantacao(e)));

        todos.sort(Comparator.comparing(DispositivoDTO::nome,
                Comparator.nullsLast(Comparator.naturalOrder())));

        int inicio = (int) pageable.getOffset();
        int fim = Math.min(inicio + pageable.getPageSize(), todos.size());
        List<DispositivoDTO> conteudo = inicio >= todos.size() ? List.of() : todos.subList(inicio, fim);
        return new PageImpl<>(conteudo, pageable, todos.size());
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
