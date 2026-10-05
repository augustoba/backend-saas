package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.CajaDtos.AbrirArqueoRequest;
import com.saasweb.restaurante.dto.CajaDtos.ArqueoResponse;
import com.saasweb.restaurante.dto.CajaDtos.CajaRequest;
import com.saasweb.restaurante.dto.CajaDtos.CajaResponse;
import com.saasweb.restaurante.dto.CajaDtos.CerrarArqueoRequest;
import com.saasweb.restaurante.dto.CajaDtos.MovimientoRequest;
import com.saasweb.restaurante.dto.CajaDtos.MovimientoResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CajaController {

    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @GetMapping("/api/restaurante/cajas")
    public List<CajaResponse> listarCajas() {
        return cajaService.listarCajas().stream().map(CajaResponse::from).toList();
    }

    @PostMapping("/api/restaurante/cajas")
    public CajaResponse crearCaja(@Valid @RequestBody CajaRequest request) {
        return CajaResponse.from(cajaService.crearCaja(request));
    }

    @PreAuthorize("hasAuthority('CAJA_VER_ARQUEO')")
    @GetMapping("/api/restaurante/arqueos/abiertos")
    public List<ArqueoResponse> arqueosAbiertos() {
        return cajaService.arqueosAbiertos();
    }

    @PreAuthorize("hasAuthority('CAJA_VER_ARQUEO')")
    @GetMapping("/api/restaurante/arqueos")
    public List<ArqueoResponse> historial() {
        return cajaService.historial();
    }

    @PreAuthorize("hasAuthority('CAJA_ABRIR')")
    @PostMapping("/api/restaurante/arqueos")
    public void abrir(Usuario usuario, @Valid @RequestBody AbrirArqueoRequest request) {
        cajaService.abrir(usuario, request);
    }

    @PreAuthorize("hasAuthority('CAJA_CERRAR')")
    @PostMapping("/api/restaurante/arqueos/{id}/cerrar")
    public void cerrar(Usuario usuario, @PathVariable Long id, @Valid @RequestBody CerrarArqueoRequest request) {
        cajaService.cerrar(usuario, id, request);
    }

    @GetMapping("/api/restaurante/movimientos-caja")
    public List<MovimientoResponse> listarMovimientos() {
        return cajaService.listarMovimientos().stream().map(MovimientoResponse::from).toList();
    }

    @PostMapping("/api/restaurante/movimientos-caja")
    public MovimientoResponse crearMovimiento(Usuario usuario, @Valid @RequestBody MovimientoRequest request) {
        return MovimientoResponse.from(cajaService.crearMovimiento(usuario, request));
    }
}
