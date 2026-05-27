package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.objects.model.CasaEntity;
import com.example.NEO_ENERGY.service.CasaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/casa")
@RequiredArgsConstructor
public class CasaController {

    private final CasaService service;

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<CasaEntity> criar(@RequestBody CasaEntity casa) {
        CasaEntity salva = service.salvar(casa);
        return ResponseEntity.created(URI.create("/casa/" + salva.getId())).body(salva);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CasaEntity>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CasaEntity> obterPorId(@PathVariable UUID id) {
        return service.obterPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pesquisar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CasaEntity>> pesquisar(
            @RequestParam(required = false) UUID idPainelSolar,
            @RequestParam(required = false) UUID idSolo) {
        return ResponseEntity.ok(service.pesquisar(idPainelSolar, idSolo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CasaEntity> atualizar(@PathVariable UUID id, @RequestBody CasaEntity casa) {
        casa.setId(id);
        return ResponseEntity.ok(service.atualizar(casa));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
