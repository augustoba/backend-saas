package com.saasweb.pos.repository;

import com.saasweb.pos.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, String> {
    List<Categoria> findByTenantIdOrderByPosicionAscNombreAsc(String tenantId);
    Optional<Categoria> findByIdAndTenantId(String id, String tenantId);
    void deleteByTenantId(String tenantId);
}
