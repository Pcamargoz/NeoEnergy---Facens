package com.example.NEO_ENERGY.objects.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioDTO(
        @NotBlank(message = "Nome Obrigatório")
        String nome,

        @NotBlank(message = "Login obrigatório")
        String login,

        @NotBlank(message = "Senha obrigatória")
        String senha,

        @NotBlank(message = "Email obrigatório")
        @Email(message = "Email inválido")
        String email
) {
}
