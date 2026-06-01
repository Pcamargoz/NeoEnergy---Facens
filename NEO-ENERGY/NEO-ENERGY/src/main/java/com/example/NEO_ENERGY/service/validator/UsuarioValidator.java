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
        validarDuplicidade(usuario.getLogin(), null);
    }

    public void validarParaAtualizar(UsuarioEntity usuario) {
        validarDuplicidade(usuario.getLogin(), usuario.getId());
    }

    private void validarDuplicidade(String username, UUID idAtual) {
        if (username == null || username.isBlank()) {
            throw new RegistroDuplicado("Username é obrigatório.");
        }

        UsuarioEntity existente = repository.findByLogin(username);
        if (existente != null && (idAtual == null || !existente.getId().equals(idAtual))) {
            throw new RegistroDuplicado("Já existe usuário com esse username.");
        }
    }
}
