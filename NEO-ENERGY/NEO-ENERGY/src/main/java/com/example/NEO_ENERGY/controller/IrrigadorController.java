package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.objects.model.IrrigadorEntity;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import com.example.NEO_ENERGY.service.IrrigadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/irrigador")
@RequiredArgsConstructor
public class IrrigadorController {

    private final IrrigadorService service;

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<IrrigadorEntity> criar(@RequestBody IrrigadorEntity irrigador) {
        IrrigadorEntity salvo = service.salvar(irrigador);
        return ResponseEntity.created(URI.create("/irrigador/" + salvo.getId())).body(salvo);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<IrrigadorEntity>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<IrrigadorEntity> obterPorId(@PathVariable UUID id) {
        return service.obterPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pesquisar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<IrrigadorEntity>> pesquisar(
            @RequestParam(required = false) STATUS_OBJETOS status,
            @RequestParam(required = false) BigDecimal aguaMin,
            @RequestParam(required = false) BigDecimal aguaMax) {
        return ResponseEntity.ok(service.pesquisar(status, aguaMin, aguaMax));
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<IrrigadorEntity> atualizar(@PathVariable UUID id, @RequestBody IrrigadorEntity irrigador) {
        irrigador.setId(id);
        return ResponseEntity.ok(service.atualizar(irrigador));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("permitAll()")
    public ResponseEntity<IrrigadorEntity> atualizarStatus(@PathVariable UUID id, @RequestParam STATUS_OBJETOS status) {
        return ResponseEntity.ok(service.atualizarStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
