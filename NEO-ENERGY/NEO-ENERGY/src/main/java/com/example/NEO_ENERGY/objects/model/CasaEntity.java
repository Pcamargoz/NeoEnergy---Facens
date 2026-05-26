package com.example.NEO_ENERGY.objects.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table
public class CasaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PsolarEntity painelSolar;

    @Column
    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private SoloEntity solo;

    @CreatedDate
    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;


}
