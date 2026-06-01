package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.dto.SessaoIrrigadorRespostaDTO;
import com.example.NEO_ENERGY.objects.model.IrrigadorEntity;
import com.example.NEO_ENERGY.service.IrrigadorService;
import com.example.NEO_ENERGY.service.SessaoIrrigadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sessoes-irrigador")
@RequiredArgsConstructor
public class SessaoIrrigadorController {

    private final SessaoIrrigadorService sessaoService;
    private final IrrigadorService irrigadorService;

    // Histórico de todas as sessões de um irrigador.
    @GetMapping("/irrigador/{irrigadorId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SessaoIrrigadorRespostaDTO>> listarPorIrrigador(@PathVariable UUID irrigadorId) {
        IrrigadorEntity irrigador = irrigadorService.obterPorId(irrigadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Irrigador não encontrado."));
        List<SessaoIrrigadorRespostaDTO> sessoes = sessaoService.listarSessoes(irrigador).stream()
                .map(SessaoIrrigadorRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(sessoes);
    }

    // Sessão aberta atual do irrigador (404 se não houver).
    @GetMapping("/irrigador/{irrigadorId}/aberta")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SessaoIrrigadorRespostaDTO> obterSessaoAberta(@PathVariable UUID irrigadorId) {
        IrrigadorEntity irrigador = irrigadorService.obterPorId(irrigadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Irrigador não encontrado."));
        return sessaoService.buscarSessaoAberta(irrigador)
                .map(SessaoIrrigadorRespostaDTO::de)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não há sessão aberta para este irrigador."));
    }
}
