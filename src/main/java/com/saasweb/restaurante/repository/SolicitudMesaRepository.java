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

public interface SolicitudMesaRepository extends JpaRepository<SolicitudMesa, Long> {

    @Query("select s from SolicitudMesa s where s.tenantId = :tenantId and s.estado = 'PENDIENTE' order by s.fecha asc")
    List<SolicitudMesa> findPendientes(@Param("tenantId") String tenantId);

    Optional<SolicitudMesa> findByIdAndTenantId(Long id, String tenantId);
}
