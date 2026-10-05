package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.OrdenDtos.AbrirMesaRequest;
import com.saasweb.restaurante.dto.OrdenDtos.AbrirMostradorRequest;
import com.saasweb.restaurante.dto.OrdenDtos.AgregarItemRequest;
import com.saasweb.restaurante.dto.OrdenDtos.DescuentoRequest;
import com.saasweb.restaurante.dto.OrdenDtos.EditarItemRequest;
import com.saasweb.restaurante.dto.OrdenDtos.OrdenResponse;
import com.saasweb.restaurante.dto.OrdenDtos.OrdenResumen;
import com.saasweb.restaurante.dto.OrdenDtos.RegistrarPagoRequest;
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
@RequestMapping("/api/restaurante/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PreAuthorize("hasAuthority('MESA_ABRIR')")
    @PostMapping("/mesa")
    public OrdenResponse abrirMesa(Usuario usuario, @Valid @RequestBody AbrirMesaRequest request) {
        Orden orden = ordenService.abrirMesa(usuario, request);
        return toResponse(orden);
    }

    @PreAuthorize("hasAuthority('ORDEN_CREAR')")
    @PostMapping("/mostrador")
    public OrdenResponse abrirMostrador(Usuario usuario, @RequestBody AbrirMostradorRequest request) {
        Orden orden = ordenService.abrirMostrador(usuario, request);
        return toResponse(orden);
    }

    @GetMapping("/mostrador")
    public List<OrdenResumen> mostradorAbiertas() {
        return ordenService.mostradorAbiertas().stream().map(OrdenResumen::from).toList();
    }

    @GetMapping("/mesa/{mesaId}")
    public OrdenResponse porMesa(@PathVariable Long mesaId) {
        return toResponse(ordenService.obtenerPorMesa(mesaId));
    }

    @GetMapping("/{id}")
    public OrdenResponse obtener(@PathVariable Long id) {
        return toResponse(ordenService.obtener(id));
    }

    @PreAuthorize("hasAuthority('ORDEN_CREAR')")
    @PostMapping("/{id}/items")
    public OrdenResponse agregarItem(@PathVariable Long id, @Valid @RequestBody AgregarItemRequest request) {
        return toResponse(ordenService.agregarItem(id, request));
    }

    @PutMapping("/{id}/items/{itemId}")
    public OrdenResponse editarItem(@PathVariable Long id, @PathVariable Long itemId, @Valid @RequestBody EditarItemRequest request) {
        return toResponse(ordenService.editarItem(id, itemId, request));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public OrdenResponse quitarItem(@PathVariable Long id, @PathVariable Long itemId) {
        return toResponse(ordenService.quitarItem(id, itemId));
    }

    @PreAuthorize("hasAuthority('ORDEN_COBRAR')")
    @PutMapping("/{id}/descuento")
    public OrdenResponse aplicarDescuento(@PathVariable Long id, @Valid @RequestBody DescuentoRequest request) {
        return toResponse(ordenService.aplicarDescuento(id, request.monto()));
    }

    @PostMapping("/{id}/confirmar")
    public OrdenResponse confirmar(@PathVariable Long id) {
        return toResponse(ordenService.confirmarItems(id));
    }

    @PostMapping("/{id}/siguiente-paso")
    public OrdenResponse siguientePaso(@PathVariable Long id) {
        return toResponse(ordenService.siguientePaso(id));
    }

    @PreAuthorize("hasAuthority('ORDEN_COBRAR')")
    @PostMapping("/{id}/pagos")
    public OrdenResponse pagar(Usuario usuario, @PathVariable Long id, @Valid @RequestBody RegistrarPagoRequest request) {
        return toResponse(ordenService.registrarPago(usuario, id, request));
    }

    @PreAuthorize("hasAuthority('ORDEN_CANCELAR')")
    @PostMapping("/{id}/cancelar")
    public OrdenResponse cancelar(@PathVariable Long id) {
        return toResponse(ordenService.cancelar(id));
    }

    private OrdenResponse toResponse(Orden orden) {
        return OrdenResponse.from(orden, ordenService.items(orden.getId()), ordenService.pagos(orden.getId()));
    }
}
