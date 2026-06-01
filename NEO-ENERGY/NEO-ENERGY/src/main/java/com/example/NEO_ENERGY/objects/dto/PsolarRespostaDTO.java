package com.example.NEO_ENERGY.objects.dto;

import com.example.NEO_ENERGY.objects.model.PsolarEntity;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PsolarRespostaDTO(
        UUID id,
        String nome,
        BigDecimal energiaPsolar,
        STATUS_OBJETOS status,
        UUID casaId,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao
) {
    public static PsolarRespostaDTO de(PsolarEntity entity) {
        UUID casaId = entity.getCasa() == null ? null : entity.getCasa().getId();
        return new PsolarRespostaDTO(
                entity.getId(),
                entity.getNome(),
                entity.getEnergiaPsolar(),
                entity.getStatus(),
                casaId,
                entity.getDataCadastro(),
                entity.getDataAtualizacao()
        );
    }
}
