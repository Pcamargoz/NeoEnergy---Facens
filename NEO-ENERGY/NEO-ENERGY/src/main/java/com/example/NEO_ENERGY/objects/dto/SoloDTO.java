package com.example.NEO_ENERGY.objects.dto;

import com.example.NEO_ENERGY.objects.model.TiposDoSolo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SoloDTO(
        @NotBlank String nomeDoSolo,
        boolean statusSolo,
        @NotNull TiposDoSolo tiposDoSolo,
        // UUID da casa a que este solo pertence (FK obrigatória).
        @NotNull UUID casaId
) {
}
