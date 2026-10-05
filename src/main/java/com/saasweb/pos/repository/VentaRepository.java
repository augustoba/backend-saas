package com.saasweb.pos.repository;

import com.saasweb.pos.model.Venta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, String> {
    Optional<Venta> findByIdAndTenantId(String id, String tenantId);
    List<Venta> findByTenantIdAndCajaIdOrderByFechaDesc(String tenantId, String cajaId);
    List<Venta> findByTenantIdOrderByFechaDesc(String tenantId, Pageable page);
    List<Venta> findByTenantId(String tenantId);
    List<Venta> findByTenantIdAndFechaBetween(String tenantId, java.time.Instant desde, java.time.Instant hasta);

    @Query("select coalesce(max(v.numero), 0) from PosVenta v where v.tenantId = :tenantId")
    int ultimoNumero(@Param("tenantId") String tenantId);
}
