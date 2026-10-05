package com.saasweb.restaurante.repository;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuOnlineConfigRepository extends JpaRepository<MenuOnlineConfig, Long> {

    Optional<MenuOnlineConfig> findByTenantId(String tenantId);

    /** La carta de ESTA tienda (la resuelve el dominio): un slug de otra tienda no se ve. */
    Optional<MenuOnlineConfig> findByTenantIdAndSlugAndActivoTrue(String tenantId, String slug);
}
