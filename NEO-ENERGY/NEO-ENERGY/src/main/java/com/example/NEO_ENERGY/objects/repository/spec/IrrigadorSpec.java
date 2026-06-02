package com.example.NEO_ENERGY.objects.repository.spec;

import com.example.NEO_ENERGY.objects.model.IrrigadorEntity;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class IrrigadorSpec {

    private IrrigadorSpec() {
        // Utility class - no instantiation
    }

    public static Specification<IrrigadorEntity> idEqual(UUID id) {
        return (root, query, cb) ->
                cb.equal(root.get("id"), id);
    }

    public static Specification<IrrigadorEntity> statusEqual(STATUS_OBJETOS status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    // Filtra irrigadores de uma casa. O irrigador não guarda a casa direto:
    // navega irrigador -> solo -> casa -> id.
    public static Specification<IrrigadorEntity> casaIdEqual(UUID casaId) {
        return (root, query, cb) ->
                cb.equal(root.get("solo").get("casa").get("id"), casaId);
    }

    public static Specification<IrrigadorEntity> aguaMaiorIgual(BigDecimal valor) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("agua"), valor);
    }

    public static Specification<IrrigadorEntity> aguaMenorIgual(BigDecimal valor) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("agua"), valor);
    }

    public static Specification<IrrigadorEntity> aguaEntre(BigDecimal min, BigDecimal max) {
        return (root, query, cb) ->
                cb.between(root.get("agua"), min, max);
    }

    public static Specification<IrrigadorEntity> dataCadastroEntre(LocalDateTime inicio, LocalDateTime fim) {
        return (root, query, cb) ->
                cb.between(root.get("dataCadastro"), inicio, fim);
    }
}
