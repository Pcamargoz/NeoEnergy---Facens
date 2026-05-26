package com.example.NEO_ENERGY.service.validator;

import com.example.NEO_ENERGY.exception.RegistroDuplicado;
import com.example.NEO_ENERGY.objects.model.UsuarioEntity;
import com.example.NEO_ENERGY.objects.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsuarioValidator {

    private final UsuarioRepository repository;

    public void validarParaCriar(UsuarioEntity usuario) {
        validarDuplicidade(usuario.getLogin(), usuario.getEmail(), null);
    }

    private void validarDuplicidade(String login, String email, UUID idAtual) {
        UsuarioEntity usuarioLogin = repository.findByLogin(login);
        if (usuarioLogin != null && (idAtual == null || !usuarioLogin.getId().equals(idAtual))) {
            throw new RegistroDuplicado("Já existe usuário com esse login.");
        }

        UsuarioEntity usuarioEmail = repository.findByEmail(email);
        if (usuarioEmail != null && (idAtual == null || !usuarioEmail.getId().equals(idAtual))) {
            throw new RegistroDuplicado("Já existe usuário com esse email.");
        }
    }

}
