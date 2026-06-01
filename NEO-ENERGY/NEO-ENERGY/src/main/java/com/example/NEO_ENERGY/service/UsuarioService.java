package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.dto.UsuarioDTO;
import com.example.NEO_ENERGY.objects.model.RoleEnum;
import com.example.NEO_ENERGY.objects.model.UsuarioEntity;
import com.example.NEO_ENERGY.objects.repository.UsuarioRepository;
import com.example.NEO_ENERGY.objects.repository.spec.UsuarioSpec;
import com.example.NEO_ENERGY.service.validator.UsuarioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioValidator validator;
    private final PasswordEncoder encoder;

    // tambem pedindo para que o usuario escolha o plano dele
    public UsuarioEntity salvar(UsuarioEntity usuario) {
        validator.validarParaCriar(usuario);
        usuario.setSenha(encoder.encode(usuario.getSenha()));
        return repository.save(usuario);
    }

    // Cria usuário a partir do DTO. Role e Casa ficam com default (NORMAL / null).
    public UsuarioEntity criarDeDTO(UsuarioDTO dto) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNome(dto.nome());
        usuario.setLogin(dto.login());
        usuario.setSenha(dto.senha());
        usuario.setEmail(dto.email());
        // Reaproveita salvar() que valida duplicidade e encoda a senha.
        return salvar(usuario);
    }

    // Atualiza login/email/nome (e re-encoda senha se vier nova).
    public UsuarioEntity atualizarDeDTO(UUID id, UsuarioDTO dto) {
        UsuarioEntity existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        existente.setNome(dto.nome());
        existente.setLogin(dto.login());
        existente.setEmail(dto.email());
        if (dto.senha() != null && !dto.senha().isBlank()) {
            existente.setSenha(encoder.encode(dto.senha()));
        }
        return repository.save(existente);
    }

    public List<UsuarioEntity> listarTodos() {
        return repository.findAll();
    }

    public Optional<UsuarioEntity> obterPorId(UUID id) {
        return repository.findById(id);
    }

    public UsuarioEntity atualizar(UsuarioEntity usuario) {
        repository.findById(usuario.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        usuario.setSenha(encoder.encode(usuario.getSenha()));
        return repository.save(usuario);
    }

    public UsuarioEntity atualizarSemReencodarSenha(UsuarioEntity usuario) {
        repository.findById(usuario.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        return repository.save(usuario);
    }

    public UsuarioEntity atualizarRole(UUID id, RoleEnum novaRole) {
        UsuarioEntity usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        usuario.setRole(novaRole);
        return repository.save(usuario);
    }

    public boolean senhaConfere(String senhaRaw, String senhaEncoded) {
        return encoder.matches(senhaRaw, senhaEncoded);
    }

    public void deletar(UUID id) {
        UsuarioEntity usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        repository.delete(usuario);
    }

    public List<UsuarioEntity> pesquisar(String username, RoleEnum role, UUID idCasa) {
        Specification<UsuarioEntity> spec = Specification.where((PredicateSpecification<UsuarioEntity>) null);
        if (username != null && !username.isBlank()) {
            spec = spec.and(UsuarioSpec.usernameLike(username));
        }
        if (role != null) {
            spec = spec.and(UsuarioSpec.roleEqual(role));
        }
        if (idCasa != null) {
            spec = spec.and(UsuarioSpec.casaIdEqual(idCasa));
        }
        return repository.findAll(spec);
    }
}
