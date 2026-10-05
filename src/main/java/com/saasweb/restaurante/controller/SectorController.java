package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.SectorDtos.SectorRequest;
import com.saasweb.restaurante.dto.SectorDtos.SectorResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurante/sectores")
public class SectorController {

    private final SectorService sectorService;

    public SectorController(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    @GetMapping
    public List<SectorResponse> listar() {
        return sectorService.listar().stream().map(SectorResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PostMapping
    public SectorResponse crear(@Valid @RequestBody SectorRequest request) {
        return SectorResponse.from(sectorService.crear(request));
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PutMapping("/{id}")
    public SectorResponse actualizar(@PathVariable Long id, @Valid @RequestBody SectorRequest request) {
        return SectorResponse.from(sectorService.actualizar(id, request));
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @DeleteMapping("/{id}")
    public void archivar(@PathVariable Long id) {
        sectorService.archivar(id);
    }
}
