package com.example.NEO_ENERGY.objects.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table
public class SessaoIrrigador {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Qual irrigador é o dono desta sessão. ManyToOne = vários ciclos para um mesmo irrigador.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "irrigador_id", nullable = false)
    private IrrigadorEntity irrigador;

    // Quanto de água foi consumido NESTE ciclo (preenchido ao fechar a sessão).
    @Column
    private BigDecimal agua;

    // Quando o irrigador foi ligado (início do ciclo).
    @Column
    private LocalDateTime tempoLigado;

    // Quando foi desligado (fim do ciclo). Enquanto a sessão estiver aberta, fica null.
    @Column
    private LocalDateTime tempoDesligado;

    // Duração deste ciclo em segundos (preenchido ao fechar a sessão).
    @Column
    private Long duracaoSegundos;

}
