package com.example.NEO_ENERGY.objects.dto;

import com.example.NEO_ENERGY.objects.model.CasaEntity;

import java.time.LocalDateTime;
import java.util.UUID;

// Resposta de Casa. Não embeda as listas de painéis/solos (coleções LAZY).
// Quem precisar dos filhos chama os endpoints de painel/solo filtrando por casaId.
public record CasaRespostaDTO(
        UUID id,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao
) {
    public static CasaRespostaDTO de(CasaEntity entity) {
        return new CasaRespostaDTO(
                entity.getId(),
                entity.getDataCadastro(),
                entity.getDataAtualizacao()
        );
    }
}
