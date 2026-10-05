package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.AutopedidoDtos.PedidoPublico;
import com.saasweb.restaurante.dto.AutopedidoDtos.ProductoAutopedido;
import com.saasweb.restaurante.dto.AutopedidoDtos.SolicitudPendiente;
import com.saasweb.restaurante.dto.OrdenDtos.AgregarItemRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AutopedidoController {

    private final AutopedidoService autopedidoService;

    public AutopedidoController(AutopedidoService autopedidoService) {
        this.autopedidoService = autopedidoService;
    }

    @GetMapping("/api/public/restaurante/pedido/{token}")
    public PedidoPublico pedido(@PathVariable String token) {
        return autopedidoService.pedido(token);
    }

    @GetMapping("/api/public/restaurante/pedido/{token}/productos")
    public List<ProductoAutopedido> catalogo(@PathVariable String token) {
        return autopedidoService.catalogo(token);
    }

    @PostMapping("/api/public/restaurante/pedido/{token}/items")
    public PedidoPublico agregarItem(@PathVariable String token, @Valid @RequestBody AgregarItemRequest request) {
        return autopedidoService.agregarItem(token, request);
    }

    @PostMapping("/api/public/restaurante/pedido/{token}/llamar-mozo")
    public void llamarMozo(@PathVariable String token) {
        autopedidoService.solicitar(token, SolicitudMesa.Tipo.LLAMAR_MOZO);
    }

    @PostMapping("/api/public/restaurante/pedido/{token}/pedir-cuenta")
    public void pedirCuenta(@PathVariable String token) {
        autopedidoService.solicitar(token, SolicitudMesa.Tipo.PEDIR_CUENTA);
    }

    @PreAuthorize("hasAuthority('SOLICITUD_MESA_ATENDER')")
    @GetMapping("/api/restaurante/solicitudes-mesa/pendientes")
    public List<SolicitudPendiente> listarSolicitudesPendientes() {
        return autopedidoService.listarPendientes().stream().map(SolicitudPendiente::from).toList();
    }

    @PreAuthorize("hasAuthority('SOLICITUD_MESA_ATENDER')")
    @PatchMapping("/api/restaurante/solicitudes-mesa/{id}/atender")
    public void atender(@PathVariable Long id) {
        autopedidoService.atender(id);
    }
}
