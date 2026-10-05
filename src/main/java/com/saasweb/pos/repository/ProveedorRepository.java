package com.saasweb.pos.repository;

import com.saasweb.pos.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository("posProveedorRepository")
public interface ProveedorRepository extends JpaRepository<Proveedor, String> {
    List<Proveedor> findByTenantIdOrderByNombreAsc(String tenantId);
    Optional<Proveedor> findByIdAndTenantId(String id, String tenantId);
    void deleteByTenantId(String tenantId);
}
