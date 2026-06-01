package com.example.NEO_ENERGY.objects.repository;

import com.example.NEO_ENERGY.objects.model.IrrigadorEntity;
import com.example.NEO_ENERGY.objects.model.SessaoIrrigador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessaoIrrigadorRepository extends JpaRepository<SessaoIrrigador, UUID> {

    // Sessão aberta = aquela cujo tempoDesligado ainda é null.
    // Spring Data monta o SQL pelo nome do método.
    Optional<SessaoIrrigador> findByIrrigadorAndTempoDesligadoIsNull(IrrigadorEntity irrigador);

    // Histórico de todos os ciclos de um irrigador.
    List<SessaoIrrigador> findByIrrigador(IrrigadorEntity irrigador);
}
