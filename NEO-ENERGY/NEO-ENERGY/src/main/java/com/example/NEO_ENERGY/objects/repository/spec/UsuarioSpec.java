package com.example.NEO_ENERGY.objects.repository.spec;

import com.example.NEO_ENERGY.objects.model.RoleEnum;
import com.example.NEO_ENERGY.objects.model.UsuarioEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class UsuarioSpec {

    private UsuarioSpec() {
        // Utility class - no instantiation
    }

    public static Specification<UsuarioEntity> usernameLike(String username) {
        return (root, query, cb) ->
                cb.like(cb.upper(root.get("username")), "%" + username.toUpperCase() + "%");
    }

    public static Specification<UsuarioEntity> roleEqual(RoleEnum role) {
        return (root, query, cb) ->
                cb.equal(root.get("role"), role);
    }

    public static Specification<UsuarioEntity> idEqual(UUID id) {
        return (root, query, cb) ->
                cb.equal(root.get("id"), id);
    }

    public static Specification<UsuarioEntity> casaIdEqual(UUID idCasa) {
        return (root, query, cb) ->
                cb.equal(root.get("casa").get("id"), idCasa);
    }

    public static Specification<UsuarioEntity> dataCadastroEntre(LocalDateTime inicio, LocalDateTime fim) {
        return (root, query, cb) ->
                cb.between(root.get("dataCadastro"), inicio, fim);
    }
}
