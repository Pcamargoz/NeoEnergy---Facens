package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.OperacaoNaoPermitidaException;
import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.dto.SoloDTO;
import com.example.NEO_ENERGY.objects.model.CasaEntity;
import com.example.NEO_ENERGY.objects.model.SoloEntity;
import com.example.NEO_ENERGY.objects.model.TiposDoSolo;
import com.example.NEO_ENERGY.objects.repository.SoloRepository;
import com.example.NEO_ENERGY.objects.repository.spec.SoloSpec;
import com.example.NEO_ENERGY.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SoloService {

    private final SoloRepository repository;
    private final AuthenticatedUserProvider authenticatedUser;

    public SoloEntity salvar(SoloEntity solo) {
        return repository.save(solo);
    }

    // Cria SEMPRE na casa do usuário logado (ignora qualquer casaId do body).
    public SoloEntity criarDeDTO(SoloDTO dto) {
        CasaEntity casa = authenticatedUser.casaAtual();
        SoloEntity solo = new SoloEntity();
        solo.setNomeDoSolo(dto.nomeDoSolo());
        solo.setStatusSolo(dto.statusSolo());
        solo.setTiposDoSolo(dto.tiposDoSolo());
        solo.setCasa(casa);
        return repository.save(solo);
    }

    public SoloEntity atualizarDeDTO(UUID id, SoloDTO dto) {
        SoloEntity existente = carregarComDono(id);
        existente.setNomeDoSolo(dto.nomeDoSolo());
        existente.setStatusSolo(dto.statusSolo());
        existente.setTiposDoSolo(dto.tiposDoSolo());
        // mantém a casa do dono.
        return repository.save(existente);
    }

    // Lista apenas as plantações (solos) da casa do usuário logado.
    public List<SoloEntity> listarTodos() {
        UUID casaId = authenticatedUser.casaIdAtualOuNull();
        if (casaId == null) {
            return List.of();
        }
        return repository.findAll(SoloSpec.casaIdEqual(casaId));
    }

    public Optional<SoloEntity> obterPorId(UUID id) {
        if (authenticatedUser.isAdmin()) {
            return repository.findById(id);
        }
        UUID casaId = authenticatedUser.casaIdAtualOuNull();
        if (casaId == null) {
            return Optional.empty();
        }
        return repository.findOne(SoloSpec.idEqual(id).and(SoloSpec.casaIdEqual(casaId)));
    }

    public List<SoloEntity> listarPorStatus(boolean statusSolo) {
        return repository.findByStatusSolo(statusSolo);
    }

    public SoloEntity atualizar(SoloEntity solo) {
        carregarComDono(solo.getId());
        return repository.save(solo);
    }

    public SoloEntity atualizarStatus(UUID id, boolean novoStatus) {
        SoloEntity solo = carregarComDono(id);
        solo.setStatusSolo(novoStatus);
        return repository.save(solo);
    }

    public void deletar(UUID id) {
        SoloEntity solo = carregarComDono(id);
        repository.delete(solo);
    }

    // Pesquisa escopada pela casa do usuário.
    public List<SoloEntity> pesquisar(Boolean statusSolo) {
        UUID casaId = authenticatedUser.casaIdAtualOuNull();
        if (casaId == null) {
            return List.of();
        }
        Specification<SoloEntity> spec = Specification.where(SoloSpec.casaIdEqual(casaId));
        if (statusSolo != null) {
            spec = spec.and(SoloSpec.statusSoloEqual(statusSolo));
        }
        return repository.findAll(spec);
    }

    // Carrega o solo garantindo que pertence à casa do usuário (ADMIN acessa qualquer um).
    private SoloEntity carregarComDono(UUID id) {
        if (authenticatedUser.isAdmin()) {
            return repository.findById(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Solo não encontrado."));
        }
        return obterPorId(id)
                .orElseThrow(() -> new OperacaoNaoPermitidaException(
                        "Solo não encontrado ou não pertence à sua casa."));
    }
}
