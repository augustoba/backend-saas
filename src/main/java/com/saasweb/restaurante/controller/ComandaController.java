package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.OrdenDtos.OrdenResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vista de "Cocina" a nivel comanda completa (Fase 18) — Tomado/Finalizado/
 * Entregado sobre CUALQUIER orden confirmada (Mesa, Mostrador o pedido
 * online), no sólo online. Separado de {@link com.saasweb.restaurante.controller.KdsController}
 * porque ese sigue el estado ítem por ítem para "enviar a cocina"; acá se
 * sigue la comanda completa para coordinar el retiro/entrega.
 */
@RestController
public class ComandaController {

    private final OrdenService ordenService;

    public ComandaController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PreAuthorize("hasAuthority('KDS_OPERAR')")
    @GetMapping("/api/restaurante/comandas")
    public List<OrdenResponse> listar() {
        return ordenService.comandasEnCocina().stream()
                .map(o -> OrdenResponse.from(o, ordenService.items(o.getId()), ordenService.pagos(o.getId())))
                .toList();
    }

    @PreAuthorize("hasAuthority('KDS_OPERAR')")
    @PatchMapping("/api/restaurante/comandas/{id}/tomar")
    public OrdenResponse tomar(@PathVariable Long id) {
        Orden orden = ordenService.marcarTomado(id);
        return OrdenResponse.from(orden, ordenService.items(orden.getId()), ordenService.pagos(orden.getId()));
    }

    @PreAuthorize("hasAuthority('KDS_OPERAR')")
    @PatchMapping("/api/restaurante/comandas/{id}/finalizar")
    public OrdenResponse finalizar(@PathVariable Long id) {
        Orden orden = ordenService.marcarListo(id);
        return OrdenResponse.from(orden, ordenService.items(orden.getId()), ordenService.pagos(orden.getId()));
    }

    @PreAuthorize("hasAuthority('KDS_OPERAR')")
    @PatchMapping("/api/restaurante/comandas/{id}/entregar")
    public OrdenResponse entregar(@PathVariable Long id) {
        Orden orden = ordenService.marcarEntregado(id);
        return OrdenResponse.from(orden, ordenService.items(orden.getId()), ordenService.pagos(orden.getId()));
    }
}
