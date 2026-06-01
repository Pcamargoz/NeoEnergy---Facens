package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.objects.dto.PsolarDTO;
import com.example.NEO_ENERGY.objects.dto.PsolarRespostaDTO;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import com.example.NEO_ENERGY.service.PsolarService;
import jakarta.validation.Valid;
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
    public ResponseEntity<PsolarRespostaDTO> criar(@RequestBody @Valid PsolarDTO dto) {
        PsolarRespostaDTO resposta = PsolarRespostaDTO.de(service.criarDeDTO(dto));
        return ResponseEntity.created(URI.create("/painel_solar/" + resposta.id())).body(resposta);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PsolarRespostaDTO>> listar() {
        List<PsolarRespostaDTO> paineis = service.listarTodos().stream()
                .map(PsolarRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(paineis);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PsolarRespostaDTO> obterPorId(@PathVariable UUID id) {
        return service.obterPorId(id)
                .map(PsolarRespostaDTO::de)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pesquisar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PsolarRespostaDTO>> pesquisar(
            @RequestParam(required = false) STATUS_OBJETOS status,
            @RequestParam(required = false) BigDecimal energiaMin,
            @RequestParam(required = false) BigDecimal energiaMax) {
        List<PsolarRespostaDTO> paineis = service.pesquisar(status, energiaMin, energiaMax).stream()
                .map(PsolarRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(paineis);
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PsolarRespostaDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid PsolarDTO dto) {
        return ResponseEntity.ok(PsolarRespostaDTO.de(service.atualizarDeDTO(id, dto)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PsolarRespostaDTO> atualizarStatus(@PathVariable UUID id, @RequestParam STATUS_OBJETOS status) {
        return ResponseEntity.ok(PsolarRespostaDTO.de(service.atualizarStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
