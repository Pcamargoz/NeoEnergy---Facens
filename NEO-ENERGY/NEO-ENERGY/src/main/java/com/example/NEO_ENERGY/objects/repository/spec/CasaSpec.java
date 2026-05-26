package com.example.NEO_ENERGY.objects.repository.spec;

import com.example.NEO_ENERGY.objects.model.CasaEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class CasaSpec {

    private CasaSpec() {
        // Utility class - no instantiation
    }

    public static Specification<CasaEntity> idEqual(UUID id) {
        return (root, query, cb) ->
                cb.equal(root.get("id"), id);
    }

    public static Specification<CasaEntity> painelSolarIdEqual(UUID idPainelSolar) {
        return (root, query, cb) ->
                cb.equal(root.get("painelSolar").get("id"), idPainelSolar);
    }

    public static Specification<CasaEntity> soloIdEqual(UUID idSolo) {
        return (root, query, cb) ->
                cb.equal(root.get("solo").get("id"), idSolo);
    }

    public static Specification<CasaEntity> dataCadastroEntre(LocalDateTime inicio, LocalDateTime fim) {
        return (root, query, cb) ->
                cb.between(root.get("dataCadastro"), inicio, fim);
    }
}
