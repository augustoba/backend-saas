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

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    Optional<Orden> findByIdAndTenantId(Long id, String tenantId);

    @Query("select o from Orden o where o.mesa.id = :mesaId and o.estado <> 'PAGADO' and o.estado <> 'CANCELADO'")
    Optional<Orden> findAbiertaPorMesa(@Param("mesaId") Long mesaId);

    @Query("select o from Orden o where o.tenantId = :tenantId and o.tipo = 'MOSTRADOR' "
            + "and o.estado <> 'PAGADO' and o.estado <> 'CANCELADO' order by o.fechaCreacion desc")
    List<Orden> findMostradorAbiertas(@Param("tenantId") String tenantId);

    @Query("select o from Orden o where o.tenantId = :tenantId order by o.fechaCreacion desc")
    List<Orden> findByTenantIdOrderByFechaCreacionDesc(@Param("tenantId") String tenantId);

    @Query("select o from Orden o where o.tokenSesion = :token and o.estado <> 'PAGADO' and o.estado <> 'CANCELADO'")
    Optional<Orden> findAbiertaPorToken(@Param("token") String token);
}
