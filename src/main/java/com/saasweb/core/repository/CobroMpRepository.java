package com.saasweb.core.repository;

import com.saasweb.core.model.CobroMp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CobroMpRepository extends JpaRepository<CobroMp, String> {

    Optional<CobroMp> findByIdAndTenantId(String id, String tenantId);

    /** Cobros creados antes de que la referencia externa fuera el id del cobro (era el del documento). */
    Optional<CobroMp> findFirstByTenantIdAndOrigenIdOrderByCreatedAtDesc(String tenantId, String origenId);

    void deleteAllByTenantId(String tenantId);
}
