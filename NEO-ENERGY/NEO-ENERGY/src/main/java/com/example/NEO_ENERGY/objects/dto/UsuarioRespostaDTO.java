package com.example.NEO_ENERGY.objects.dto;

import com.example.NEO_ENERGY.objects.model.RoleEnum;
import com.example.NEO_ENERGY.objects.model.UsuarioEntity;

import java.time.LocalDateTime;
import java.util.UUID;

// IMPORTANTE: este DTO NÃO inclui a senha. É a única forma segura de retornar usuário.
public record UsuarioRespostaDTO(
        UUID id,
        String login,
        String email,
        String nome,
        RoleEnum role,
        UUID casaId,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao
) {
    public static UsuarioRespostaDTO de(UsuarioEntity entity) {
        // entity.getCasa() é LAZY, mas getId() no proxy não dispara loading.
        UUID casaId = entity.getCasa() == null ? null : entity.getCasa().getId();
        return new UsuarioRespostaDTO(
                entity.getId(),
                entity.getLogin(),
                entity.getEmail(),
                entity.getNome(),
                entity.getRole(),
                casaId,
                entity.getDataCadastro(),
                entity.getDataAtualizacao()
        );
    }
}
