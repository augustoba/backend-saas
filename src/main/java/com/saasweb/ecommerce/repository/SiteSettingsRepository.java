package com.saasweb.ecommerce.repository;

import com.saasweb.ecommerce.model.SiteSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSettingsRepository extends JpaRepository<SiteSettings, String> {
}
