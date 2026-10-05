package com.saasweb.pos.repository;

import com.saasweb.pos.model.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@org.springframework.stereotype.Repository("posMovimientoCajaRepository")
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, String> {
    List<MovimientoCaja> findByTenantIdAndCajaIdOrderByFechaAsc(String tenantId, String cajaId);
    void deleteByTenantId(String tenantId);
}
