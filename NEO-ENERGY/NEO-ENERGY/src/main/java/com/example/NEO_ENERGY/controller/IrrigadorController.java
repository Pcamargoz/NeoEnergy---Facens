package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.objects.dto.IrrigadorDTO;
import com.example.NEO_ENERGY.objects.dto.IrrigadorRespostaDTO;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import com.example.NEO_ENERGY.service.IrrigadorService;
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
@RequestMapping("/irrigador")
@RequiredArgsConstructor
public class IrrigadorController {

    private final IrrigadorService service;

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<IrrigadorRespostaDTO> criar(@RequestBody @Valid IrrigadorDTO dto) {
        IrrigadorRespostaDTO resposta = IrrigadorRespostaDTO.de(service.criarDeDTO(dto));
        return ResponseEntity.created(URI.create("/irrigador/" + resposta.id())).body(resposta);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<IrrigadorRespostaDTO>> listar() {
        List<IrrigadorRespostaDTO> irrigadores = service.listarTodos().stream()
                .map(IrrigadorRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(irrigadores);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<IrrigadorRespostaDTO> obterPorId(@PathVariable UUID id) {
        return service.obterPorId(id)
                .map(IrrigadorRespostaDTO::de)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pesquisar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<IrrigadorRespostaDTO>> pesquisar(
            @RequestParam(required = false) STATUS_OBJETOS status,
            @RequestParam(required = false) BigDecimal aguaMin,
            @RequestParam(required = false) BigDecimal aguaMax) {
        List<IrrigadorRespostaDTO> irrigadores = service.pesquisar(status, aguaMin, aguaMax).stream()
                .map(IrrigadorRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(irrigadores);
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<IrrigadorRespostaDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid IrrigadorDTO dto) {
        return ResponseEntity.ok(IrrigadorRespostaDTO.de(service.atualizarDeDTO(id, dto)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("permitAll()")
    public ResponseEntity<IrrigadorRespostaDTO> atualizarStatus(@PathVariable UUID id, @RequestParam STATUS_OBJETOS status) {
        return ResponseEntity.ok(IrrigadorRespostaDTO.de(service.atualizarStatus(id, status)));
    }

    // Body inline: o front manda se o clima está apto (consulta de previsão/sensor antes de chamar).
    public record LigarRequest(@jakarta.validation.constraints.NotNull Boolean climaApto) {}

    @PostMapping("/{id}/ligar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<IrrigadorRespostaDTO> ligar(@PathVariable UUID id,
                                                      @RequestBody @Valid LigarRequest req) {
        return ResponseEntity.ok(IrrigadorRespostaDTO.de(service.ligarRegador(id, req.climaApto())));
    }

    @PostMapping("/{id}/desligar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<IrrigadorRespostaDTO> desligar(@PathVariable UUID id) {
        return ResponseEntity.ok(IrrigadorRespostaDTO.de(service.desligarRegador(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
