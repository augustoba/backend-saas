package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.PreferenciasDtos.PreferenciasRequest;
import com.saasweb.restaurante.dto.PreferenciasDtos.PreferenciasResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurante/preferencias")
public class PreferenciasController {

    private final PreferenciasService preferenciasService;

    public PreferenciasController(PreferenciasService preferenciasService) {
        this.preferenciasService = preferenciasService;
    }

    @GetMapping
    public PreferenciasResponse obtener() {
        return PreferenciasResponse.from(preferenciasService.obtenerOCrear());
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PutMapping
    public PreferenciasResponse actualizar(@Valid @RequestBody PreferenciasRequest request) {
        return PreferenciasResponse.from(preferenciasService.actualizar(request));
    }
}
