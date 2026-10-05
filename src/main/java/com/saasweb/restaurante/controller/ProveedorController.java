package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ProveedorDtos.ProveedorRequest;
import com.saasweb.restaurante.dto.ProveedorDtos.ProveedorResponse;
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
@RequestMapping("/api/restaurante/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @PreAuthorize("hasAuthority('PROVEEDOR_VER')")
    @GetMapping
    public List<ProveedorResponse> listar() {
        return proveedorService.listar().stream().map(ProveedorResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('PROVEEDOR_CREAR')")
    @PostMapping
    public ProveedorResponse crear(@Valid @RequestBody ProveedorRequest request) {
        return ProveedorResponse.from(proveedorService.crear(request));
    }

    @PreAuthorize("hasAuthority('PROVEEDOR_CREAR')")
    @PutMapping("/{id}")
    public ProveedorResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorRequest request) {
        return ProveedorResponse.from(proveedorService.actualizar(id, request));
    }

    @PreAuthorize("hasAuthority('PROVEEDOR_CREAR')")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        proveedorService.eliminar(id);
    }
}
