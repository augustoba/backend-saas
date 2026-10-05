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

public interface ArqueoRepository extends JpaRepository<Arqueo, Long> {

    @Query("select a from Arqueo a where a.caja.tenantId = :tenantId and a.estado = 'ABIERTO'")
    List<Arqueo> findAbiertosPorTenant(@Param("tenantId") String tenantId);

    Optional<Arqueo> findByCajaIdAndEstado(Long cajaId, Arqueo.Estado estado);

    @Query("select a from Arqueo a where a.caja.tenantId = :tenantId order by a.fechaApertura desc")
    List<Arqueo> findHistorialPorTenant(@Param("tenantId") String tenantId);

    @Query("select a from Arqueo a where a.id = :id and a.caja.tenantId = :tenantId")
    Optional<Arqueo> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") String tenantId);
}
