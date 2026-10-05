package com.saasweb.core.repository;

import com.saasweb.core.model.Dominio;
import com.saasweb.core.model.EstadoDominio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DominioRepository extends JpaRepository<Dominio, String> {

    Optional<Dominio> findByHost(String host);

    Optional<Dominio> findByHostAndEstado(String host, EstadoDominio estado);

    List<Dominio> findByTenantIdOrderByCreatedAtAsc(String tenantId);

    Optional<Dominio> findFirstByTenantIdAndPrincipalTrueAndEstado(String tenantId, EstadoDominio estado);

    Optional<Dominio> findByIdAndTenantId(String id, String tenantId);

    void deleteByTenantId(String tenantId);
}
