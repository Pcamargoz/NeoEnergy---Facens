package com.example.NEO_ENERGY.service.validator;

import com.example.NEO_ENERGY.exception.OperacaoNaoPermitidaException;
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

    // Valida a troca de plano: só checa (não altera nada). Exige que o usuário tenha confirmado.
    // A mudança de role em si fica no service, que é quem deve alterar o estado.
    public void validarParaTrocarPlano(boolean confirmacao) {
        if (!confirmacao) {
            throw new OperacaoNaoPermitidaException("É necessário confirmar a troca de plano.");
        }
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
