package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.dto.CasaDTO;
import com.example.NEO_ENERGY.objects.dto.DispositivoDTO;
import com.example.NEO_ENERGY.objects.model.CasaEntity;
import com.example.NEO_ENERGY.objects.model.TipoDispositivo;
import com.example.NEO_ENERGY.objects.model.UsuarioEntity;
import com.example.NEO_ENERGY.objects.repository.CasaRepository;
import com.example.NEO_ENERGY.objects.repository.IrrigadorRepository;
import com.example.NEO_ENERGY.objects.repository.PsolarRepository;
import com.example.NEO_ENERGY.objects.repository.SoloRepository;
import com.example.NEO_ENERGY.objects.repository.UsuarioRepository;
import com.example.NEO_ENERGY.objects.repository.spec.CasaSpec;
import com.example.NEO_ENERGY.objects.repository.spec.IrrigadorSpec;
import com.example.NEO_ENERGY.objects.repository.spec.PsolarSpec;
import com.example.NEO_ENERGY.objects.repository.spec.SoloSpec;
import com.example.NEO_ENERGY.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    private final UsuarioRepository usuarioRepository;
    private final AuthenticatedUserProvider authenticatedUser;

    public CasaEntity salvar(CasaEntity casa) {
        return repository.save(casa);
    }

    // POST /casa: cria a casa do usuário logado e vincula a ele.
    // Como é 1 usuário = 1 casa (OneToOne), se já existir, devolve a existente.
    public CasaEntity criarParaUsuario() {
        UsuarioEntity usuario = authenticatedUser.usuarioAtual();
        if (usuario.getCasa() != null) {
            return usuario.getCasa();
        }
        CasaEntity casa = repository.save(new CasaEntity());
        usuario.setCasa(casa);
        usuarioRepository.save(usuario);
        return casa;
    }

    // Casa do usuário autenticado (404 se ainda não tem).
    public CasaEntity minhaCasa() {
        return authenticatedUser.casaAtual();
    }

    // Lista os dispositivos de uma casa — só se a casa for do usuário (ou ADMIN).
    public Page<DispositivoDTO> listarDispositivos(UUID casaId, TipoDispositivo tipo, Pageable pageable) {
        authenticatedUser.exigirDonoDaCasa(casaId);
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

    // Atualizar uma casa sem campos próprios é praticamente um no-op; valida dono + existência.
    public CasaEntity atualizarDeDTO(UUID id, CasaDTO dto) {
        authenticatedUser.exigirDonoDaCasa(id);
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Casa não encontrada."));
    }

    // Lista só a casa do usuário (0 ou 1).
    public List<CasaEntity> listarTodos() {
        CasaEntity casa = authenticatedUser.usuarioAtual().getCasa();
        return casa == null ? List.of() : List.of(casa);
    }

    public Optional<CasaEntity> obterPorId(UUID id) {
        if (authenticatedUser.isAdmin()) {
            return repository.findById(id);
        }
        UUID minha = authenticatedUser.casaIdAtualOuNull();
        if (minha == null || !minha.equals(id)) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    public CasaEntity atualizar(CasaEntity casa) {
        authenticatedUser.exigirDonoDaCasa(casa.getId());
        repository.findById(casa.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Casa não encontrada."));
        return repository.save(casa);
    }

    public void deletar(UUID id) {
        authenticatedUser.exigirDonoDaCasa(id);
        // Desvincula a casa do usuário dono antes de apagar (evita FK pendente).
        UsuarioEntity usuario = authenticatedUser.usuarioAtual();
        if (usuario.getCasa() != null && usuario.getCasa().getId().equals(id)) {
            usuario.setCasa(null);
            usuarioRepository.save(usuario);
        }
        CasaEntity casa = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Casa não encontrada."));
        repository.delete(casa);
    }

    // Pesquisa escopada: usuário comum só enxerga a própria casa.
    public List<CasaEntity> pesquisar(UUID idPainelSolar, UUID idSolo) {
        List<Specification<CasaEntity>> filtros = new ArrayList<>();
        if (!authenticatedUser.isAdmin()) {
            UUID minha = authenticatedUser.casaIdAtualOuNull();
            if (minha == null) {
                return List.of();
            }
            filtros.add(CasaSpec.idEqual(minha));
        }
        if (idPainelSolar != null) {
            filtros.add(CasaSpec.painelSolarIdEqual(idPainelSolar));
        }
        if (idSolo != null) {
            filtros.add(CasaSpec.soloIdEqual(idSolo));
        }
        if (filtros.isEmpty()) {
            return repository.findAll();
        }
        Specification<CasaEntity> spec = filtros.get(0);
        for (int i = 1; i < filtros.size(); i++) {
            spec = spec.and(filtros.get(i));
        }
        return repository.findAll(spec);
    }
}
