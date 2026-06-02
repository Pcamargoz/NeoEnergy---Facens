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
public class IrrigadorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column
    private STATUS_OBJETOS status;

    // Vamos usar para acumular o quanto de agua devemos pagar
    @Column
    private BigDecimal agua;

    // Lado dono do relacionamento com SoloEntity (cada irrigador pertence a um solo).
    // A casa do irrigador vem por aqui: irrigador -> solo -> casa (sem campo casa redundante).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solo_id", nullable = false)
    private SoloEntity solo;

    @CreatedDate
    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;


    // Soma de quantos segundos o irrigador já ficou ligado no total (todos os ciclos).
    // O instante de quando ligou/desligou agora fica em SessaoIrrigador.
    @Column
    private Long tempoTotalLigadoSegundos;

}
