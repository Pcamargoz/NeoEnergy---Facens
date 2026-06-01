package com.example.NEO_ENERGY.objects.dto;

// Casa não tem campos escalares próprios (só id + timestamps automáticos + relações).
// Este DTO existe só como "marker" do contrato de entrada — corpo da request pode vir vazio {}.
// Painéis e solos são gerenciados pelos endpoints respectivos, não embedados aqui.
public record CasaDTO() {
}
