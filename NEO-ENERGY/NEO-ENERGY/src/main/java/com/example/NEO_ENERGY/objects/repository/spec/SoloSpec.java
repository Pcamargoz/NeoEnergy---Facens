package com.example.NEO_ENERGY.objects.repository.spec;

import com.example.NEO_ENERGY.objects.model.SoloEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class SoloSpec {

    private SoloSpec() {
        // Utility class - no instantiation
    }

    public static Specification<SoloEntity> idEqual(UUID id) {
        return (root, query, cb) ->
                cb.equal(root.get("id"), id);
    }

    public static Specification<SoloEntity> statusSoloEqual(boolean statusSolo) {
        return (root, query, cb) ->
                cb.equal(root.get("statusSolo"), statusSolo);
    }

    // Filtra solos (plantações) de uma casa específica (campo casa -> id).
    public static Specification<SoloEntity> casaIdEqual(UUID casaId) {
        return (root, query, cb) ->
                cb.equal(root.get("casa").get("id"), casaId);
    }

    public static Specification<SoloEntity> plantacoesMaiorIgual(BigDecimal valor) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("plantacoes"), valor);
    }

    public static Specification<SoloEntity> plantacoesMenorIgual(BigDecimal valor) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("plantacoes"), valor);
    }

    public static Specification<SoloEntity> plantacoesEntre(BigDecimal min, BigDecimal max) {
        return (root, query, cb) ->
                cb.between(root.get("plantacoes"), min, max);
    }

    public static Specification<SoloEntity> dataCadastroEntre(LocalDateTime inicio, LocalDateTime fim) {
        return (root, query, cb) ->
                cb.between(root.get("dataCadastro"), inicio, fim);
    }
}
