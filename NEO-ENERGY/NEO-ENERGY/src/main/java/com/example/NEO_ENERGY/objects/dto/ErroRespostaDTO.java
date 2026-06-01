package com.example.NEO_ENERGY.objects.dto;

import java.util.List;

public record ErroRespostaDTO(int status, String mensagem, List<ErroCampoDTO> erros) {
    public static ErroRespostaDTO conflito(String mensagem) {
        return new ErroRespostaDTO(409, mensagem, List.of());
    }
}
