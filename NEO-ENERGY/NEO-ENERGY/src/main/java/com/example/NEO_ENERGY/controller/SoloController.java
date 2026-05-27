package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.objects.model.SoloEntity;
import com.example.NEO_ENERGY.service.SoloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/solo")
@RequiredArgsConstructor
public class SoloController {

    private final SoloService service;

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<SoloEntity> criar(@RequestBody SoloEntity solo) {
        SoloEntity salvo = service.salvar(solo);
        return ResponseEntity.created(URI.create("/solo/" + salvo.getId())).body(salvo);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SoloEntity>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SoloEntity> obterPorId(@PathVariable UUID id) {
        return service.obterPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pesquisar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SoloEntity>> pesquisar(
            @RequestParam(required = false) Boolean statusSolo,
            @RequestParam(required = false) BigDecimal plantacoesMin,
            @RequestParam(required = false) BigDecimal plantacoesMax) {
        return ResponseEntity.ok(service.pesquisar(statusSolo, plantacoesMin, plantacoesMax));
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SoloEntity> atualizar(@PathVariable UUID id, @RequestBody SoloEntity solo) {
        solo.setId(id);
        return ResponseEntity.ok(service.atualizar(solo));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SoloEntity> atualizarStatus(@PathVariable UUID id, @RequestParam boolean status) {
        return ResponseEntity.ok(service.atualizarStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
