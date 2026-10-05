package com.saasweb.core.repository;

import com.saasweb.core.model.PlatformMailSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformMailSettingsRepository extends JpaRepository<PlatformMailSettings, String> {
}
