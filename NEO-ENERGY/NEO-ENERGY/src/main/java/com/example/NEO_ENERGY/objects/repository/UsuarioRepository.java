package com.example.NEO_ENERGY.objects.repository;

import com.example.NEO_ENERGY.objects.model.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsuarioRepository extends
        JpaRepository<UsuarioEntity, UUID>,
        JpaSpecificationExecutor<UsuarioEntity> {

    UsuarioEntity findByLogin(String login);
    UsuarioEntity findByEmail(String email);
}
