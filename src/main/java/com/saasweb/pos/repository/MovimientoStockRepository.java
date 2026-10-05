package com.saasweb.pos.repository;

import com.saasweb.pos.model.MovimientoStock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, String> {
    List<MovimientoStock> findByTenantIdOrderByFechaDesc(String tenantId, Pageable page);
    List<MovimientoStock> findByTenantIdAndArticuloIdOrderByFechaDesc(String tenantId, String articuloId, Pageable page);
    void deleteByTenantId(String tenantId);
}
