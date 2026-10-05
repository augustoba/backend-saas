package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.GastoDtos.AsignarProveedorRequest;
import com.saasweb.restaurante.dto.GastoDtos.CategoriaGastoRequest;
import com.saasweb.restaurante.dto.GastoDtos.CategoriaGastoResponse;
import com.saasweb.restaurante.dto.GastoDtos.ClasificacionRequest;
import com.saasweb.restaurante.dto.GastoDtos.ClasificacionResponse;
import com.saasweb.restaurante.dto.GastoDtos.GastoKpis;
import com.saasweb.restaurante.dto.GastoDtos.GastoRequest;
import com.saasweb.restaurante.dto.GastoDtos.GastoResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GastoController {

    private final GastoService gastoService;

    public GastoController(GastoService gastoService) {
        this.gastoService = gastoService;
    }

    @GetMapping("/api/restaurante/clasificaciones-gasto")
    public List<ClasificacionResponse> listarClasificaciones() {
        return gastoService.listarClasificaciones().stream().map(ClasificacionResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PostMapping("/api/restaurante/clasificaciones-gasto")
    public ClasificacionResponse crearClasificacion(@Valid @RequestBody ClasificacionRequest request) {
        return ClasificacionResponse.from(gastoService.crearClasificacion(request));
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @DeleteMapping("/api/restaurante/clasificaciones-gasto/{id}")
    public void archivarClasificacion(@PathVariable Long id) {
        gastoService.archivarClasificacion(id);
    }

    @GetMapping("/api/restaurante/categorias-gasto")
    public List<CategoriaGastoResponse> listarCategorias() {
        return gastoService.listarCategorias().stream().map(CategoriaGastoResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PostMapping("/api/restaurante/categorias-gasto")
    public CategoriaGastoResponse crearCategoria(@Valid @RequestBody CategoriaGastoRequest request) {
        return CategoriaGastoResponse.from(gastoService.crearCategoria(request));
    }

    @PreAuthorize("hasAuthority('GASTO_VER')")
    @GetMapping("/api/restaurante/gastos")
    public List<GastoResponse> listar() {
        return gastoService.listar().stream().map(GastoResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('GASTO_VER')")
    @GetMapping("/api/restaurante/gastos/kpis")
    public GastoKpis kpis() {
        return gastoService.kpis();
    }

    @PreAuthorize("hasAuthority('GASTO_CREAR')")
    @PostMapping("/api/restaurante/gastos")
    public GastoResponse crear(@Valid @RequestBody GastoRequest request) {
        return GastoResponse.from(gastoService.crear(request));
    }

    @PreAuthorize("hasAuthority('GASTO_CREAR')")
    @PatchMapping("/api/restaurante/gastos/{id}/pagar")
    public GastoResponse marcarPagado(@PathVariable Long id) {
        return GastoResponse.from(gastoService.marcarPagado(id));
    }

    @PreAuthorize("hasAuthority('GASTO_VER')")
    @GetMapping("/api/restaurante/gastos/sin-asignar")
    public List<GastoResponse> listarSinAsignar() {
        return gastoService.listarSinAsignar().stream().map(GastoResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('GASTO_CREAR')")
    @PatchMapping("/api/restaurante/gastos/{id}/asignar-proveedor")
    public GastoResponse asignarProveedor(@PathVariable Long id, @Valid @RequestBody AsignarProveedorRequest request) {
        return GastoResponse.from(gastoService.asignarProveedor(id, request.proveedorId()));
    }
}
