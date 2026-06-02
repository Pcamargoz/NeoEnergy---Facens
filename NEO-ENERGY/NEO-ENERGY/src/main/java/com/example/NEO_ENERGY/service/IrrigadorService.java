package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.OperacaoNaoPermitidaException;
import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.dto.IrrigadorDTO;
import com.example.NEO_ENERGY.objects.model.IrrigadorEntity;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import com.example.NEO_ENERGY.objects.model.SessaoIrrigador;
import com.example.NEO_ENERGY.objects.model.SoloEntity;
import com.example.NEO_ENERGY.objects.repository.IrrigadorRepository;
import com.example.NEO_ENERGY.objects.repository.SoloRepository;
import com.example.NEO_ENERGY.objects.repository.spec.IrrigadorSpec;
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
public class IrrigadorService {

    private final IrrigadorRepository repository;
    private final SessaoIrrigadorService sessaoService;
    private final SoloRepository soloRepository;
    private final AuthenticatedUserProvider authenticatedUser;

    public IrrigadorEntity salvar(IrrigadorEntity irrigador) {
        return repository.save(irrigador);
    }

    // Cria o irrigador num solo que precisa pertencer à casa do usuário logado.
    public IrrigadorEntity criarDeDTO(IrrigadorDTO dto) {
        SoloEntity solo = soloRepository.findById(dto.soloId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solo não encontrado: " + dto.soloId()));
        authenticatedUser.exigirDonoDaCasa(solo.getCasa().getId());
        IrrigadorEntity irrigador = new IrrigadorEntity();
        irrigador.setNome(dto.nome());
        irrigador.setStatus(dto.status());
        irrigador.setSolo(solo);
        return repository.save(irrigador);
    }

    public IrrigadorEntity atualizarDeDTO(UUID id, IrrigadorDTO dto) {
        IrrigadorEntity existente = carregarComDono(id);
        SoloEntity solo = soloRepository.findById(dto.soloId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solo não encontrado: " + dto.soloId()));
        authenticatedUser.exigirDonoDaCasa(solo.getCasa().getId());
        existente.setNome(dto.nome());
        existente.setStatus(dto.status());
        existente.setSolo(solo);
        return repository.save(existente);
    }

    // Lista apenas os irrigadores da casa do usuário logado.
    public List<IrrigadorEntity> listarTodos() {
        UUID casaId = authenticatedUser.casaIdAtualOuNull();
        if (casaId == null) {
            return List.of();
        }
        return repository.findAll(IrrigadorSpec.casaIdEqual(casaId));
    }

    public Optional<IrrigadorEntity> obterPorId(UUID id) {
        if (authenticatedUser.isAdmin()) {
            return repository.findById(id);
        }
        UUID casaId = authenticatedUser.casaIdAtualOuNull();
        if (casaId == null) {
            return Optional.empty();
        }
        return repository.findOne(IrrigadorSpec.idEqual(id).and(IrrigadorSpec.casaIdEqual(casaId)));
    }

    public List<IrrigadorEntity> listarPorStatus(STATUS_OBJETOS status) {
        return repository.findByStatus(status);
    }

    public IrrigadorEntity atualizar(IrrigadorEntity irrigador) {
        carregarComDono(irrigador.getId());
        return repository.save(irrigador);
    }

    public IrrigadorEntity atualizarStatus(UUID id, STATUS_OBJETOS novoStatus) {
        IrrigadorEntity irrigador = carregarComDono(id);
        irrigador.setStatus(novoStatus);
        return repository.save(irrigador);
    }

    // climaApto vem do front (consulta de tempo/sensor). Se false, a sessão é recusada lá no SessaoService.
    public IrrigadorEntity ligarRegador(UUID id, boolean climaApto) {
        IrrigadorEntity irrigador = carregarComDono(id);

        if (irrigador.getStatus() == STATUS_OBJETOS.LIGADO) {
            throw new IllegalStateException("Irrigador já está ligado.");
        }

        // Delega a criação da sessão (e a validação do clima) para o service de sessão.
        sessaoService.abrirSessao(irrigador, climaApto);
        irrigador.setStatus(STATUS_OBJETOS.LIGADO);

        return repository.save(irrigador);
    }

    public IrrigadorEntity desligarRegador(UUID id) {
        IrrigadorEntity irrigador = carregarComDono(id);

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
        IrrigadorEntity irrigador = carregarComDono(id);
        repository.delete(irrigador);
    }

    // Pesquisa escopada pela casa do usuário + filtros atuais.
    public List<IrrigadorEntity> pesquisar(STATUS_OBJETOS status, BigDecimal aguaMin, BigDecimal aguaMax) {
        UUID casaId = authenticatedUser.casaIdAtualOuNull();
        if (casaId == null) {
            return List.of();
        }
        Specification<IrrigadorEntity> spec = Specification.where(IrrigadorSpec.casaIdEqual(casaId));
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

    // Carrega o irrigador garantindo que pertence à casa do usuário (ADMIN acessa qualquer um).
    private IrrigadorEntity carregarComDono(UUID id) {
        if (authenticatedUser.isAdmin()) {
            return repository.findById(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Irrigador não encontrado."));
        }
        return obterPorId(id)
                .orElseThrow(() -> new OperacaoNaoPermitidaException(
                        "Irrigador não encontrado ou não pertence à sua casa."));
    }
}
