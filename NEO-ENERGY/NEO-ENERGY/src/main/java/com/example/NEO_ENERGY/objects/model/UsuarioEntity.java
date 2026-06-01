package com.example.NEO_ENERGY.objects.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table
@EntityListeners(AuditingEntityListener.class)
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "login", length = 50, nullable = false, unique = true)
    private String login;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "senha", length = 255, nullable = false)
    private String senha;

    @Column(name = "nome", length = 150)
    private String nome;

    // quando cadastra a conta ele ja automaticamente vem com essa role
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private RoleEnum role = RoleEnum.NORMAL;


    // OneToOne: cada usuário tem uma casa. LAZY evita JOIN automático em todo SELECT de usuário.
    // O @JoinColumn diz qual coluna da tabela usuario guarda a FK pra casa.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_id")
    private CasaEntity casa;


    @CreatedDate
    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

}
