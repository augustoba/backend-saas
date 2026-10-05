package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.MedioPagoDtos.MedioPagoRequest;
import com.saasweb.restaurante.dto.MedioPagoDtos.MedioPagoResponse;
import com.saasweb.restaurante.dto.MedioPagoDtos.ReordenarRequest;
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
@RequestMapping("/api/restaurante/medios-pago")
public class MedioPagoController {

    private final MedioPagoService medioPagoService;

    public MedioPagoController(MedioPagoService medioPagoService) {
        this.medioPagoService = medioPagoService;
    }

    @GetMapping
    public List<MedioPagoResponse> listar() {
        return medioPagoService.listar().stream().map(MedioPagoResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PostMapping
    public MedioPagoResponse crear(@Valid @RequestBody MedioPagoRequest request) {
        return MedioPagoResponse.from(medioPagoService.crear(request));
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PutMapping("/{id}")
    public MedioPagoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MedioPagoRequest request) {
        return MedioPagoResponse.from(medioPagoService.actualizar(id, request));
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PutMapping("/reordenar")
    public void reordenar(@Valid @RequestBody ReordenarRequest request) {
        medioPagoService.reordenar(request.ids());
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @DeleteMapping("/{id}")
    public void archivar(@PathVariable Long id) {
        medioPagoService.archivar(id);
    }
}
