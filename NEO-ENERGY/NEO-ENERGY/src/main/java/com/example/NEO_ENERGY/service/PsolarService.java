package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.OperacaoNaoPermitidaException;
import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.dto.PsolarDTO;
import com.example.NEO_ENERGY.objects.model.CasaEntity;
import com.example.NEO_ENERGY.objects.model.PsolarEntity;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import com.example.NEO_ENERGY.objects.repository.PsolarRepository;
import com.example.NEO_ENERGY.objects.repository.spec.PsolarSpec;
import com.example.NEO_ENERGY.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
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
    private final AuthenticatedUserProvider authenticatedUser;

    public PsolarEntity salvar(PsolarEntity psolar) {
        return repository.save(psolar);
    }

    // Cria SEMPRE na casa do usuário logado (ignora qualquer casaId do body).
    public PsolarEntity criarDeDTO(PsolarDTO dto) {
        CasaEntity casa = authenticatedUser.casaAtual();
        PsolarEntity psolar = new PsolarEntity();
        psolar.setNome(dto.nome());
        psolar.setEnergiaPsolar(dto.energia());
        psolar.setStatus(dto.status());
        psolar.setCasa(casa);
        return repository.save(psolar);
    }

    public PsolarEntity atualizarDeDTO(UUID id, PsolarDTO dto) {
        PsolarEntity existente = carregarComDono(id);
        existente.setNome(dto.nome());
        existente.setEnergiaPsolar(dto.energia());
        existente.setStatus(dto.status());
        // não troca a casa: o painel continua na casa do dono.
        return repository.save(existente);
    }

    // Lista apenas os painéis da casa do usuário logado.
    public List<PsolarEntity> listarTodos() {
        UUID casaId = authenticatedUser.casaIdAtualOuNull();
        if (casaId == null) {
            return List.of();
        }
        return repository.findAll(PsolarSpec.casaIdEqual(casaId));
    }

    // Retorna o painel só se for da casa do usuário (ou se for ADMIN).
    public Optional<PsolarEntity> obterPorId(UUID id) {
        if (authenticatedUser.isAdmin()) {
            return repository.findById(id);
        }
        UUID casaId = authenticatedUser.casaIdAtualOuNull();
        if (casaId == null) {
            return Optional.empty();
        }
        return repository.findOne(PsolarSpec.idEqual(id).and(PsolarSpec.casaIdEqual(casaId)));
    }

    public List<PsolarEntity> listarPorStatus(STATUS_OBJETOS status) {
        return repository.findByStatus(status);
    }

    public PsolarEntity atualizar(PsolarEntity psolar) {
        carregarComDono(psolar.getId());
        return repository.save(psolar);
    }

    // Atualiza SOMENTE a energia do painel (do dono).
    public PsolarEntity atualizarEnergia(PsolarEntity psolar){
        PsolarEntity existente = carregarComDono(psolar.getId());
        existente.setEnergiaPsolar(psolar.getEnergiaPsolar());
        return repository.save(existente);
    }

    public PsolarEntity atualizarStatus(UUID id, STATUS_OBJETOS novoStatus) {
        PsolarEntity psolar = carregarComDono(id);
        psolar.setStatus(novoStatus);
        return repository.save(psolar);
    }

    public void deletar(UUID id) {
        PsolarEntity psolar = carregarComDono(id);
        repository.delete(psolar);
    }

    // Pesquisa escopada pela casa do usuário + os filtros atuais.
    public List<PsolarEntity> pesquisar(STATUS_OBJETOS status, BigDecimal energiaMin, BigDecimal energiaMax) {
        UUID casaId = authenticatedUser.casaIdAtualOuNull();
        if (casaId == null) {
            return List.of();
        }
        Specification<PsolarEntity> spec = Specification.where(PsolarSpec.casaIdEqual(casaId));
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

    // Carrega o painel garantindo que pertence à casa do usuário (ADMIN acessa qualquer um).
    // Lança 403 se não for do dono, 404 se não existir.
    private PsolarEntity carregarComDono(UUID id) {
        if (authenticatedUser.isAdmin()) {
            return repository.findById(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Painel solar não encontrado."));
        }
        return obterPorId(id)
                .orElseThrow(() -> new OperacaoNaoPermitidaException(
                        "Painel solar não encontrado ou não pertence à sua casa."));
    }
}
