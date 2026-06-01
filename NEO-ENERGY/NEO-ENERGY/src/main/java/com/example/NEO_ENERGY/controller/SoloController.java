package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.objects.dto.SoloDTO;
import com.example.NEO_ENERGY.objects.dto.SoloRespostaDTO;
import com.example.NEO_ENERGY.service.SoloService;
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
@RequestMapping("/solo")
@RequiredArgsConstructor
public class SoloController {

    private final SoloService service;

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<SoloRespostaDTO> criar(@RequestBody @Valid SoloDTO dto) {
        SoloRespostaDTO resposta = SoloRespostaDTO.de(service.criarDeDTO(dto));
        return ResponseEntity.created(URI.create("/solo/" + resposta.id())).body(resposta);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SoloRespostaDTO>> listar() {
        List<SoloRespostaDTO> solos = service.listarTodos().stream()
                .map(SoloRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(solos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SoloRespostaDTO> obterPorId(@PathVariable UUID id) {
        return service.obterPorId(id)
                .map(SoloRespostaDTO::de)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pesquisar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SoloRespostaDTO>> pesquisar(
            @RequestParam(required = false) Boolean statusSolo,
            @RequestParam(required = false) BigDecimal plantacoesMin,
            @RequestParam(required = false) BigDecimal plantacoesMax) {
        List<SoloRespostaDTO> solos = service.pesquisar(statusSolo, plantacoesMin, plantacoesMax).stream()
                .map(SoloRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(solos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SoloRespostaDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid SoloDTO dto) {
        return ResponseEntity.ok(SoloRespostaDTO.de(service.atualizarDeDTO(id, dto)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SoloRespostaDTO> atualizarStatus(@PathVariable UUID id, @RequestParam boolean status) {
        return ResponseEntity.ok(SoloRespostaDTO.de(service.atualizarStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
