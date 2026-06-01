package com.example.NEO_ENERGY.objects.repository;

import com.example.NEO_ENERGY.objects.model.RefreshTokenEntity;
import com.example.NEO_ENERGY.objects.model.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByToken(String token);

    // Todos os tokens ativos de um usuário — usado pelo logout pra revogar de uma vez.
    List<RefreshTokenEntity> findAllByUsuarioAndRevogadoFalse(UsuarioEntity usuario);
}
