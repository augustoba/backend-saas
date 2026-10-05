package com.saasweb.ecommerce.repository;

import com.saasweb.ecommerce.model.ParamGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParamRepository extends JpaRepository<ParamGroup, String> {
    Optional<ParamGroup> findByIdAndTenantId(String id, String tenantId);
    List<ParamGroup> findByTenantId(String tenantId);

    /** Borra las parametrías del tenant (y sus opciones, cascade ALL/orphanRemoval) — ver TenantDeletionService. */
    void deleteAllByTenantId(String tenantId);
}
