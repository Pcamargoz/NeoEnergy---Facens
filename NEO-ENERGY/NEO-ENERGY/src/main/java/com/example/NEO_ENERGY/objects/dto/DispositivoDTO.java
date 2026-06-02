package com.example.NEO_ENERGY.objects.dto;

import com.example.NEO_ENERGY.objects.model.IrrigadorEntity;
import com.example.NEO_ENERGY.objects.model.PsolarEntity;
import com.example.NEO_ENERGY.objects.model.SoloEntity;
import com.example.NEO_ENERGY.objects.model.TipoDispositivo;

import java.util.UUID;

// DTO resumido de um dispositivo de casa, comum aos três tipos.
// Mostra só o essencial pra uma lista/cards; detalhes vêm do endpoint específico de cada tipo.
public record DispositivoDTO(
        UUID id,
        TipoDispositivo tipo,
        String nome,
        String status
) {
    public static DispositivoDTO deIrrigador(IrrigadorEntity e) {
        String status = e.getStatus() == null ? null : e.getStatus().name();
        return new DispositivoDTO(e.getId(), TipoDispositivo.IRRIGADOR, e.getNome(), status);
    }

    public static DispositivoDTO dePainel(PsolarEntity e) {
        String status = e.getStatus() == null ? null : e.getStatus().name();
        return new DispositivoDTO(e.getId(), TipoDispositivo.PAINEL_SOLAR, e.getNome(), status);
    }

    public static DispositivoDTO dePlantacao(SoloEntity e) {
        // Para a plantação, o "status" mais útil é o tipo do solo (SECO/MOLHADO).
        String status = e.getTiposDoSolo() == null ? null : e.getTiposDoSolo().name();
        return new DispositivoDTO(e.getId(), TipoDispositivo.PLANTACAO, e.getNomeDoSolo(), status);
    }
}
