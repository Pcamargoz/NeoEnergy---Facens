package com.example.NEO_ENERGY.objects.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "refresh_token")
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // String opaca aleatória (não é JWT). Validar exige hit no banco.
    @Column(nullable = false, unique = true, length = 128)
    private String token;

    // Dono do refresh token. LAZY porque normalmente só precisamos do ID.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    // Soft-delete: na rotação ou no logout marcamos como revogado em vez de apagar.
    // Permite auditoria depois ("quando foi revogado, por quê").
    @Column(nullable = false)
    private boolean revogado;
}
