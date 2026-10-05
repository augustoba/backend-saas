package com.saasweb.restaurante.repository;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionReservaSalonRepository extends JpaRepository<ConfiguracionReservaSalon, Long> {
    Optional<ConfiguracionReservaSalon> findBySalonId(Long salonId);
    List<ConfiguracionReservaSalon> findAll();
}
