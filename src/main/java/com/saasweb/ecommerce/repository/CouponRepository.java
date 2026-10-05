package com.saasweb.ecommerce.repository;

import com.saasweb.ecommerce.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, String> {
    Optional<Coupon> findByIdAndTenantId(String id, String tenantId);
    Optional<Coupon> findByTenantIdAndCodeIgnoreCase(String tenantId, String code);
    boolean existsByTenantIdAndCodeIgnoreCase(String tenantId, String code);
    List<Coupon> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    /** Ver TenantDeletionService. */
    void deleteAllByTenantId(String tenantId);
}
