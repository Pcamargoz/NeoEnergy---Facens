package com.example.NEO_ENERGY.objects.repository.spec;

import com.example.NEO_ENERGY.objects.model.PsolarEntity;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PsolarSpec {

    private PsolarSpec() {
        // Utility class - no instantiation
    }

    public static Specification<PsolarEntity> idEqual(UUID id) {
        return (root, query, cb) ->
                cb.equal(root.get("id"), id);
    }

    public static Specification<PsolarEntity> statusEqual(STATUS_OBJETOS status) {
        return (root, query, cb) ->
                cb.equal(root.get("Status"), status);
    }

    // Filtra painéis de uma casa específica (campo casa -> id).
    public static Specification<PsolarEntity> casaIdEqual(UUID casaId) {
        return (root, query, cb) ->
                cb.equal(root.get("casa").get("id"), casaId);
    }

    public static Specification<PsolarEntity> energiaMaiorIgual(BigDecimal valor) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("energiaPsolar"), valor);
    }

    public static Specification<PsolarEntity> energiaMenorIgual(BigDecimal valor) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("energiaPsolar"), valor);
    }

    public static Specification<PsolarEntity> energiaEntre(BigDecimal min, BigDecimal max) {
        return (root, query, cb) ->
                cb.between(root.get("energiaPsolar"), min, max);
    }

    public static Specification<PsolarEntity> dataCadastroEntre(LocalDateTime inicio, LocalDateTime fim) {
        return (root, query, cb) ->
                cb.between(root.get("dataCadastro"), inicio, fim);
    }
}
