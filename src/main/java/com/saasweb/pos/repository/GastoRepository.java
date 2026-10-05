package com.saasweb.pos.repository;

import com.saasweb.pos.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GastoRepository extends JpaRepository<Gasto, String> {
    List<Gasto> findByTenantIdAndFechaBetweenOrderByFechaDescCreadoEnDesc(String tenantId, LocalDate desde, LocalDate hasta);
    Optional<Gasto> findByIdAndTenantId(String id, String tenantId);
    long countByTenantIdAndProveedorId(String tenantId, String proveedorId);
    void deleteByTenantId(String tenantId);
}
