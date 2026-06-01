package com.example.NEO_ENERGY.objects.dto;

import com.example.NEO_ENERGY.objects.model.SoloEntity;
import com.example.NEO_ENERGY.objects.model.TiposDoSolo;

import java.time.LocalDateTime;
import java.util.UUID;

public record SoloRespostaDTO(
        UUID id,
        String nomeDoSolo,
        boolean statusSolo,
        TiposDoSolo tiposDoSolo,
        UUID casaId,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao
) {
    public static SoloRespostaDTO de(SoloEntity entity) {
        UUID casaId = entity.getCasa() == null ? null : entity.getCasa().getId();
        return new SoloRespostaDTO(
                entity.getId(),
                entity.getNomeDoSolo(),
                entity.isStatusSolo(),
                entity.getTiposDoSolo(),
                casaId,
                entity.getDataCadastro(),
                entity.getDataAtualizacao()
        );
    }
}
