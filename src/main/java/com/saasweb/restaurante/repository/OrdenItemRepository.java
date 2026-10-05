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

public interface OrdenItemRepository extends JpaRepository<OrdenItem, Long> {

    List<OrdenItem> findByOrdenIdOrderByIdAsc(Long ordenId);

    Optional<OrdenItem> findByIdAndOrdenId(Long id, Long ordenId);

    @Query("select i from OrdenItem i where i.orden.tenantId = :tenantId "
            + "and i.orden.estado in (com.saasweb.restaurante.model.Orden.Estado.EN_CURSO, com.saasweb.restaurante.model.Orden.Estado.CONFIRMADO) "
            + "and i.estado <> com.saasweb.restaurante.model.OrdenItem.Estado.ENTREGADO "
            + "order by i.orden.fechaCreacion asc, i.id asc")
    List<OrdenItem> findParaCocina(@Param("tenantId") String tenantId);

    Optional<OrdenItem> findByIdAndOrdenTenantId(Long id, String tenantId);

    List<OrdenItem> findByOrdenTenantIdAndOrdenEstado(String tenantId, Orden.Estado estado);
}
