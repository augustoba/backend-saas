package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.OrdenDtos.AbrirOnlineRequest;
import com.saasweb.restaurante.dto.OrdenDtos.OrdenResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pedidos creados por el cliente desde la carta pública (con handoff a
 * WhatsApp del lado del front) — separado de {@link OrdenController} porque
 * el de creación es público y el resto reusa los endpoints normales de
 * Órdenes (confirmar = aceptar, cancelar = rechazar).
 */
@RestController
public class PedidoOnlineController {

    private final OrdenService ordenService;

    public PedidoOnlineController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PostMapping("/api/public/restaurante/pedidos-online")
    public OrdenResponse crear(@Valid @RequestBody AbrirOnlineRequest request) {
        Orden orden = ordenService.abrirOnline(request);
        return OrdenResponse.from(orden, ordenService.items(orden.getId()), ordenService.pagos(orden.getId()));
    }

    @PreAuthorize("hasAuthority('ORDEN_CREAR')")
    @GetMapping("/api/restaurante/pedidos-online/pendientes")
    public List<OrdenResponse> pendientes() {
        return ordenService.pedidosOnlinePendientes().stream()
                .map(o -> OrdenResponse.from(o, ordenService.items(o.getId()), ordenService.pagos(o.getId())))
                .toList();
    }
}
