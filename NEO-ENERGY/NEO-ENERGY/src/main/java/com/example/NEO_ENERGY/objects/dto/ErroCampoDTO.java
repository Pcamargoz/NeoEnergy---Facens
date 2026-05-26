package com.example.NEO_ENERGY.objects.dto;

import java.util.List;

public record ErroCampoDTO(int status, String message, List<ErroCampoDTO> erros) {
}
