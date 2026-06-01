package com.example.NEO_ENERGY.objects.model;


import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table
public class PsolarEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String nome;

    @Column
    private BigDecimal energiaPsolar;

    // @Enumerated(STRING) faz o JPA salvar "LIGADO"/"DESLIGADO" em vez de 0/1.
    @Enumerated(EnumType.STRING)
    @Column
    private STATUS_OBJETOS Status;

    // Lado dono do relacionamento com CasaEntity. Aqui mora a coluna casa_id no banco.
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
