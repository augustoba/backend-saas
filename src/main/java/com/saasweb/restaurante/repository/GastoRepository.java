package com.saasweb.restaurante.repository;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GastoRepository extends JpaRepository<Gasto, Long> {
    List<Gasto> findByTenantIdOrderByFechaDesc(String tenantId);
    Optional<Gasto> findByIdAndTenantId(Long id, String tenantId);
    List<Gasto> findByTenantIdAndProveedorIsNullOrderByFechaDesc(String tenantId);

    @Query("select count(g) from Gasto g where g.tenantId = :tenantId and g.estado = 'PENDIENTE'")
    long countAVencer(@Param("tenantId") String tenantId);

    @Query("select count(g) from Gasto g where g.tenantId = :tenantId and g.estado = 'VENCIDO'")
    long countVencidos(@Param("tenantId") String tenantId);
}
