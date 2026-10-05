package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.MesaDtos.MesaEstadoRequest;
import com.saasweb.restaurante.dto.MesaDtos.MesaRequest;
import com.saasweb.restaurante.dto.MesaDtos.MesaResponse;
import com.saasweb.restaurante.dto.MesaDtos.SalonConMesas;
import com.saasweb.restaurante.dto.MesaDtos.SalonRequest;
import com.saasweb.restaurante.dto.MesaDtos.SalonResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping("/api/restaurante/salones")
    public List<SalonConMesas> listarSalones() {
        return mesaService.listarSalonesConMesas();
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PostMapping("/api/restaurante/salones")
    public SalonResponse crearSalon(@Valid @RequestBody SalonRequest request) {
        return SalonResponse.from(mesaService.crearSalon(request));
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PostMapping("/api/restaurante/mesas")
    public MesaResponse crearMesa(@Valid @RequestBody MesaRequest request) {
        return MesaResponse.from(mesaService.crearMesa(request));
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PutMapping("/api/restaurante/mesas/{id}")
    public MesaResponse actualizarMesa(@PathVariable Long id, @Valid @RequestBody MesaRequest request) {
        return MesaResponse.from(mesaService.actualizarMesa(id, request));
    }

    @PreAuthorize("hasAuthority('MESA_ABRIR')")
    @PatchMapping("/api/restaurante/mesas/{id}/estado")
    public MesaResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody MesaEstadoRequest request) {
        return MesaResponse.from(mesaService.cambiarEstado(id, request.estado()));
    }
}
