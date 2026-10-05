package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ZonaEnvioDtos.ZonaEnvioRequest;
import com.saasweb.restaurante.dto.ZonaEnvioDtos.ZonaEnvioResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ZonaEnvioController {

    private final ZonaEnvioService zonaEnvioService;

    public ZonaEnvioController(ZonaEnvioService zonaEnvioService) {
        this.zonaEnvioService = zonaEnvioService;
    }

    @GetMapping("/api/restaurante/zonas-envio")
    public List<ZonaEnvioResponse> listar() {
        return zonaEnvioService.listar().stream().map(ZonaEnvioResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PostMapping("/api/restaurante/zonas-envio")
    public ZonaEnvioResponse crear(@Valid @RequestBody ZonaEnvioRequest request) {
        return ZonaEnvioResponse.from(zonaEnvioService.crear(request));
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PutMapping("/api/restaurante/zonas-envio/{id}")
    public ZonaEnvioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ZonaEnvioRequest request) {
        return ZonaEnvioResponse.from(zonaEnvioService.actualizar(id, request));
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @DeleteMapping("/api/restaurante/zonas-envio/{id}")
    public void archivar(@PathVariable Long id) {
        zonaEnvioService.archivar(id);
    }
}
