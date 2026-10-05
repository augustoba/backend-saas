package com.saasweb.core.repository;

import com.saasweb.core.model.Comprobante;
import com.saasweb.core.model.Sistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprobanteRepository extends JpaRepository<Comprobante, String> {

    List<Comprobante> findByTenantIdAndOrigenSistemaAndOrigenIdAndClaseOrderByCreatedAtDesc(
            String tenantId, Sistema origenSistema, String origenId, Comprobante.Clase clase);

    void deleteAllByTenantId(String tenantId);
}
