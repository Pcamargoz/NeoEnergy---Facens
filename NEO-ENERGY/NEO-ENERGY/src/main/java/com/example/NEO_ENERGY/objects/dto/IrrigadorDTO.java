package com.example.NEO_ENERGY.objects.dto;

import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IrrigadorDTO(
        @NotBlank String nome,
        @NotNull STATUS_OBJETOS status,
        // UUID do solo a que este irrigador pertence (FK obrigatória na entidade).
        @NotNull UUID soloId
) {
}
