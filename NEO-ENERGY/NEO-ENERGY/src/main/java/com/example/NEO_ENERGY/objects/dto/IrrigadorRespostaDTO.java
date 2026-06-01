package com.example.NEO_ENERGY.objects.dto;

import com.example.NEO_ENERGY.objects.model.IrrigadorEntity;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record IrrigadorRespostaDTO(
        UUID id,
        String nome,
        STATUS_OBJETOS status,
        BigDecimal agua,
        Long tempoTotalLigadoSegundos,
        UUID soloId,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao
) {
    public static IrrigadorRespostaDTO de(IrrigadorEntity entity) {
        UUID soloId = entity.getSolo() == null ? null : entity.getSolo().getId();
        return new IrrigadorRespostaDTO(
                entity.getId(),
                entity.getNome(),
                entity.getStatus(),
                entity.getAgua(),
                entity.getTempoTotalLigadoSegundos(),
                soloId,
                entity.getDataCadastro(),
                entity.getDataAtualizacao()
        );
    }
}
