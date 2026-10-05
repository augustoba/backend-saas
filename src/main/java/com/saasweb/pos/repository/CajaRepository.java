package com.saasweb.pos.repository;

import com.saasweb.pos.model.Caja;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository("posCajaRepository")
public interface CajaRepository extends JpaRepository<Caja, String> {
    Optional<Caja> findFirstByTenantIdAndEstado(String tenantId, Caja.Estado estado);
    Optional<Caja> findByIdAndTenantId(String id, String tenantId);
    List<Caja> findByTenantIdOrderByAbiertaEnDesc(String tenantId, Pageable page);

    @Query("select coalesce(max(c.numero), 0) from PosCaja c where c.tenantId = :tenantId")
    int ultimoNumero(@Param("tenantId") String tenantId);

    void deleteByTenantId(String tenantId);
}
