package com.example.NEO_ENERGY.objects.dto;

import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

// Energia é só leitura via front; na criação envia o valor inicial.
public record PsolarDTO(
        @NotBlank String nome,
        @NotNull BigDecimal energia,
        @NotNull STATUS_OBJETOS status,
        // UUID da casa a que este painel pertence (FK obrigatória).
        @NotNull UUID casaId
) {
}
