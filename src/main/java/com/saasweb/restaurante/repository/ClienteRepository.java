package com.saasweb.restaurante.repository;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdAndTenantId(Long id, String tenantId);

    @Query("select c from Cliente c where c.tenantId = :tenantId and "
            + "(:q is null or lower(c.nombre) like concat('%', :q, '%') "
            + "or lower(c.email) like concat('%', :q, '%') or c.telefono like concat('%', :q, '%'))")
    Page<Cliente> buscar(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);

    long countByTenantId(String tenantId);

    long countByTenantIdAndActivoTrue(String tenantId);

    long countByTenantIdAndTieneCuentaCorrienteTrue(String tenantId);

    @Query("select count(c) from Cliente c where c.tenantId = :tenantId and c.descuentoPorcentaje is not null and c.descuentoPorcentaje > 0")
    long countConDescuento(@Param("tenantId") String tenantId);
}
