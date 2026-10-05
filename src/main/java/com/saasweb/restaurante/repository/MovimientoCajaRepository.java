package com.saasweb.restaurante.repository;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    List<MovimientoCaja> findByArqueoIdOrderByFechaDesc(Long arqueoId);

    @Query("select m from MovimientoCaja m where m.arqueo.caja.tenantId = :tenantId order by m.fecha desc")
    List<MovimientoCaja> findByTenantIdOrderByFechaDesc(@Param("tenantId") String tenantId);
}
