package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.objects.dto.UsuarioDTO;
import com.example.NEO_ENERGY.objects.dto.UsuarioRespostaDTO;
import com.example.NEO_ENERGY.objects.model.RoleEnum;
import com.example.NEO_ENERGY.security.AuthenticatedUserProvider;
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
    private final AuthenticatedUserProvider authenticatedUser;

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

    // Atualização de perfil: cada usuário só pode editar a SI MESMO (id do path == id do
    // autenticado). ADMIN pode editar qualquer um. Sem isso, qualquer autenticado poderia
    // alterar o perfil de outro usuário passando o id alheio na URL.
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioRespostaDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid UsuarioDTO dto) {
        authenticatedUser.exigirProprioOuAdmin(id);
        return ResponseEntity.ok(UsuarioRespostaDTO.de(service.atualizarDeDTO(id, dto)));
    }

    // Troca de plano: o próprio usuário (autenticado) sobe pra PRO, mediante confirmação.
    // Restrito ao dono (ou ADMIN) — ninguém pode alterar o plano de outra conta.
    @PatchMapping("/{id}/plano")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioRespostaDTO> trocarPlano(
            @PathVariable UUID id,
            @RequestParam boolean confirmacao) {
        authenticatedUser.exigirProprioOuAdmin(id);
        return ResponseEntity.ok(UsuarioRespostaDTO.de(service.atualizarRole(id, confirmacao)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
