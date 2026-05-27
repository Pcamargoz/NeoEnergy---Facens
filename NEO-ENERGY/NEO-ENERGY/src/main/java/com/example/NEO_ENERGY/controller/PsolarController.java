package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.objects.model.PsolarEntity;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import com.example.NEO_ENERGY.service.PsolarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/painel_solar")
@RequiredArgsConstructor
public class PsolarController {

    private final PsolarService service;

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<PsolarEntity> criar(@RequestBody PsolarEntity psolar) {
        PsolarEntity salvo = service.salvar(psolar);
        return ResponseEntity.created(URI.create("/painel_solar/" + salvo.getId())).body(salvo);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PsolarEntity>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PsolarEntity> obterPorId(@PathVariable UUID id) {
        return service.obterPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pesquisar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PsolarEntity>> pesquisar(
            @RequestParam(required = false) STATUS_OBJETOS status,
            @RequestParam(required = false) BigDecimal energiaMin,
            @RequestParam(required = false) BigDecimal energiaMax) {
        return ResponseEntity.ok(service.pesquisar(status, energiaMin, energiaMax));
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PsolarEntity> atualizar(@PathVariable UUID id, @RequestBody PsolarEntity psolar) {
        psolar.setId(id);
        return ResponseEntity.ok(service.atualizar(psolar));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PsolarEntity> atualizarStatus(@PathVariable UUID id, @RequestParam STATUS_OBJETOS status) {
        return ResponseEntity.ok(service.atualizarStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
