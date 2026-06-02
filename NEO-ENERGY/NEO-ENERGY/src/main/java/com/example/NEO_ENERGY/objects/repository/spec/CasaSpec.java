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

    // paineisSolares e solos são coleções (@OneToMany), então filtra-se com JOIN, não com get().
    // distinct evita casas repetidas quando a coleção tem vários itens.
    public static Specification<CasaEntity> painelSolarIdEqual(UUID idPainelSolar) {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.join("paineisSolares").get("id"), idPainelSolar);
        };
    }

    public static Specification<CasaEntity> soloIdEqual(UUID idSolo) {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.join("solos").get("id"), idSolo);
        };
    }

    public static Specification<CasaEntity> dataCadastroEntre(LocalDateTime inicio, LocalDateTime fim) {
        return (root, query, cb) ->
                cb.between(root.get("dataCadastro"), inicio, fim);
    }
}
