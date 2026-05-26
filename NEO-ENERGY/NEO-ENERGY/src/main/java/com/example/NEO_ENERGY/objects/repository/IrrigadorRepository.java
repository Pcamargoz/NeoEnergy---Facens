package com.example.NEO_ENERGY.objects.repository;

import com.example.NEO_ENERGY.objects.model.IrrigadorEntity;
import com.example.NEO_ENERGY.objects.model.STATUS_OBJETOS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IrrigadorRepository extends
        JpaRepository<IrrigadorEntity, UUID>,
        JpaSpecificationExecutor<IrrigadorEntity> {

    List<IrrigadorEntity> findByStatus(STATUS_OBJETOS status);
}
