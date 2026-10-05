package com.saasweb.pos.repository;

import com.saasweb.pos.model.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticuloRepository extends JpaRepository<Articulo, String> {
    List<Articulo> findByTenantIdAndArchivadoOrderByNombreAsc(String tenantId, boolean archivado);
    Optional<Articulo> findByIdAndTenantId(String id, String tenantId);
    Optional<Articulo> findByTenantIdAndCodigoBarras(String tenantId, String codigoBarras);
    long countByTenantIdAndCategoriaId(String tenantId, String categoriaId);
    void deleteByTenantId(String tenantId);
}
