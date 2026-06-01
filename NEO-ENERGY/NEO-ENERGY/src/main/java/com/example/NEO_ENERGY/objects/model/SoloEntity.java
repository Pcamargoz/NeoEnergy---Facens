package com.example.NEO_ENERGY.objects.model;


import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table
public class SoloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String nomeDoSolo;

    // vai ser usado para saber se esta com chuva ou não
    // fazer o metodo para essa variavel aqui
    @Column
    private boolean statusSolo;

    @Enumerated(EnumType.STRING)
    @Column
    private TiposDoSolo tiposDoSolo;

    // 1 solo tem vários irrigadores. mappedBy aponta pro campo "solo" em IrrigadorEntity (lado dono).
    @OneToMany(mappedBy = "solo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<IrrigadorEntity> irrigadores = new ArrayList<>();

    // Lado dono do relacionamento com CasaEntity.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "casa_id", nullable = false)
    private CasaEntity casa;

    @CreatedDate
    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;




}
