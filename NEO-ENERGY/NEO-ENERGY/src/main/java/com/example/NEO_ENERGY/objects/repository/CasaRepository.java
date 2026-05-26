package com.example.NEO_ENERGY.objects.repository;

import com.example.NEO_ENERGY.objects.model.CasaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CasaRepository extends
        JpaRepository<CasaEntity, UUID>,
        JpaSpecificationExecutor<CasaEntity> {
}
