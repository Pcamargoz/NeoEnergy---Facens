package com.example.NEO_ENERGY.objects.repository;

import com.example.NEO_ENERGY.objects.model.SoloEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SoloRepository extends
        JpaRepository<SoloEntity, UUID>,
        JpaSpecificationExecutor<SoloEntity> {

    List<SoloEntity> findByStatusSolo(boolean statusSolo);
}
