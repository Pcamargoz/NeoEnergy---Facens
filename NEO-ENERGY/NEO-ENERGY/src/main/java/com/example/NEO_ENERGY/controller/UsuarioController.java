package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.objects.dto.UsuarioDTO;
import com.example.NEO_ENERGY.objects.dto.UsuarioRespostaDTO;
import com.example.NEO_ENERGY.objects.model.RoleEnum;
import com.example.NEO_ENERGY.service.UsuarioService;
import jakarta.validation.Valid;
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
    public ResponseEntity<UsuarioRespostaDTO> criar(@RequestBody @Valid UsuarioDTO dto) {
        UsuarioRespostaDTO resposta = UsuarioRespostaDTO.de(service.criarDeDTO(dto));
        return ResponseEntity.created(URI.create("/usuarios/" + resposta.id())).body(resposta);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioRespostaDTO>> listar() {
        List<UsuarioRespostaDTO> usuarios = service.listarTodos().stream()
                .map(UsuarioRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioRespostaDTO> obterPorId(@PathVariable UUID id) {
        return service.obterPorId(id)
                .map(UsuarioRespostaDTO::de)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pesquisar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioRespostaDTO>> pesquisar(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) RoleEnum role,
            @RequestParam(required = false) UUID idCasa) {
        List<UsuarioRespostaDTO> usuarios = service.pesquisar(username, role, idCasa).stream()
                .map(UsuarioRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    // Qualquer usuário autenticado pode atualizar dados. TODO futuro: restringir
    // pra que cada um só edite o próprio perfil (comparar principal.id == path id).
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioRespostaDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid UsuarioDTO dto) {
        return ResponseEntity.ok(UsuarioRespostaDTO.de(service.atualizarDeDTO(id, dto)));
    }

    // Trocar role é privilégio administrativo — só ADMIN pode promover/rebaixar.
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioRespostaDTO> atualizarRole(@PathVariable UUID id, @RequestParam RoleEnum role) {
        return ResponseEntity.ok(UsuarioRespostaDTO.de(service.atualizarRole(id, role)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
