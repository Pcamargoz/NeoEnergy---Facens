package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.objects.dto.CasaDTO;
import com.example.NEO_ENERGY.objects.dto.CasaRespostaDTO;
import com.example.NEO_ENERGY.service.CasaService;
import jakarta.validation.Valid;
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
    public ResponseEntity<CasaRespostaDTO> criar(@RequestBody @Valid CasaDTO dto) {
        CasaRespostaDTO resposta = CasaRespostaDTO.de(service.criarDeDTO(dto));
        return ResponseEntity.created(URI.create("/casa/" + resposta.id())).body(resposta);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CasaRespostaDTO>> listar() {
        List<CasaRespostaDTO> casas = service.listarTodos().stream()
                .map(CasaRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(casas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CasaRespostaDTO> obterPorId(@PathVariable UUID id) {
        return service.obterPorId(id)
                .map(CasaRespostaDTO::de)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pesquisar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CasaRespostaDTO>> pesquisar(
            @RequestParam(required = false) UUID idPainelSolar,
            @RequestParam(required = false) UUID idSolo) {
        List<CasaRespostaDTO> casas = service.pesquisar(idPainelSolar, idSolo).stream()
                .map(CasaRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(casas);
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CasaRespostaDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid CasaDTO dto) {
        return ResponseEntity.ok(CasaRespostaDTO.de(service.atualizarDeDTO(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
