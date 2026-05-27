package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.objects.model.RoleEnum;
import com.example.NEO_ENERGY.objects.model.UsuarioEntity;
import com.example.NEO_ENERGY.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<UsuarioEntity> criar(@RequestBody UsuarioEntity usuario) {
        UsuarioEntity salvo = service.salvar(usuario);
        return ResponseEntity.created(URI.create("/usuarios/" + salvo.getId())).body(salvo);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioEntity>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioEntity> obterPorId(@PathVariable UUID id) {
        return service.obterPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pesquisar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioEntity>> pesquisar(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) RoleEnum role,
            @RequestParam(required = false) UUID idCasa) {
        return ResponseEntity.ok(service.pesquisar(username, role, idCasa));
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<UsuarioEntity> atualizar(@PathVariable UUID id, @RequestBody UsuarioEntity usuario) {
        usuario.setId(id);
        return ResponseEntity.ok(service.atualizar(usuario));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("permitAll()")
    public ResponseEntity<UsuarioEntity> atualizarRole(@PathVariable UUID id, @RequestParam RoleEnum role) {
        return ResponseEntity.ok(service.atualizarRole(id, role));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
