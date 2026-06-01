package com.example.NEO_ENERGY.objects.dto;

import com.example.NEO_ENERGY.objects.model.SessaoIrrigador;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Resposta de uma sessão de irrigação. Só expõe o irrigadorId (não a entidade inteira)
// para evitar LazyInitializationException e loops de serialização.
public record SessaoIrrigadorRespostaDTO(
        UUID id,
        UUID irrigadorId,
        BigDecimal agua,
        LocalDateTime tempoLigado,
        LocalDateTime tempoDesligado,
        Long duracaoSegundos
) {
    public static SessaoIrrigadorRespostaDTO de(SessaoIrrigador entity) {
        UUID irrigadorId = entity.getIrrigador() == null ? null : entity.getIrrigador().getId();
        return new SessaoIrrigadorRespostaDTO(
                entity.getId(),
                irrigadorId,
                entity.getAgua(),
                entity.getTempoLigado(),
                entity.getTempoDesligado(),
                entity.getDuracaoSegundos()
        );
    }
}
