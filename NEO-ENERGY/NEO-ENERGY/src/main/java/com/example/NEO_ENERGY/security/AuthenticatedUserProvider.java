package com.example.NEO_ENERGY.security;

import com.example.NEO_ENERGY.exception.OperacaoNaoPermitidaException;
import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.model.CasaEntity;
import com.example.NEO_ENERGY.objects.model.RoleEnum;
import com.example.NEO_ENERGY.objects.model.UsuarioEntity;
import com.example.NEO_ENERGY.objects.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utilitário central para obter o usuário autenticado a partir do SecurityContext.
 * O subject do JWT é o login; aqui resolvemos o login para a UsuarioEntity correspondente.
 *
 * Componente isolado: não altera comportamento existente, apenas oferece um ponto único
 * para escopar dados/operações pelo dono. Pode ser injetado em qualquer controller/service.
 */
@Component
@RequiredArgsConstructor
public class AuthenticatedUserProvider {

    private final UsuarioRepository usuarioRepository;

    /** Retorna o login do usuário autenticado, ou null se não houver autenticação. */
    public String loginAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    /** Retorna a UsuarioEntity autenticada. Lança se não houver autenticação válida. */
    public UsuarioEntity usuarioAtual() {
        String login = loginAtual();
        if (login == null) {
            throw new OperacaoNaoPermitidaException("Nenhum usuário autenticado.");
        }
        UsuarioEntity usuario = usuarioRepository.findByLogin(login);
        if (usuario == null) {
            throw new OperacaoNaoPermitidaException("Usuário autenticado não encontrado.");
        }
        return usuario;
    }

    /** True se o usuário autenticado tem a role ADMIN. */
    public boolean isAdmin() {
        return usuarioAtual().getRole() == RoleEnum.ADMIN;
    }

    /**
     * Garante que o usuário autenticado é o dono do recurso (mesmo id) ou um ADMIN.
     * Lança OperacaoNaoPermitidaException (HTTP 403) caso contrário.
     */
    public void exigirProprioOuAdmin(UUID idAlvo) {
        UsuarioEntity atual = usuarioAtual();
        if (atual.getRole() == RoleEnum.ADMIN) {
            return;
        }
        if (!atual.getId().equals(idAlvo)) {
            throw new OperacaoNaoPermitidaException(
                    "Você só pode alterar os seus próprios dados.");
        }
    }

    /** Casa do usuário autenticado. Lança 404 se ele ainda não cadastrou uma casa. */
    public CasaEntity casaAtual() {
        CasaEntity casa = usuarioAtual().getCasa();
        if (casa == null) {
            throw new RecursoNaoEncontradoException("Usuário ainda não possui uma casa cadastrada.");
        }
        return casa;
    }

    /** Id da casa do usuário, ou null se ele ainda não tem casa (usado para listagens vazias). */
    public UUID casaIdAtualOuNull() {
        CasaEntity casa = usuarioAtual().getCasa();
        return casa == null ? null : casa.getId();
    }

    /**
     * Garante que a casa informada é a do usuário autenticado (ADMIN passa direto).
     * Lança 403 caso contrário. Usado para escopar acessos por id ao dono.
     */
    public void exigirDonoDaCasa(UUID casaId) {
        if (isAdmin()) {
            return;
        }
        UUID minha = casaIdAtualOuNull();
        if (minha == null || !minha.equals(casaId)) {
            throw new OperacaoNaoPermitidaException("Esse recurso não pertence à sua casa.");
        }
    }
}
