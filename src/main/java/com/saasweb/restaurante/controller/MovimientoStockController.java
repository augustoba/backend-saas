package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.MovimientoStockDtos.InventarioItem;
import com.saasweb.restaurante.dto.MovimientoStockDtos.MovimientoStockRequest;
import com.saasweb.restaurante.dto.MovimientoStockDtos.MovimientoStockResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;

    public MovimientoStockController(MovimientoStockService movimientoStockService) {
        this.movimientoStockService = movimientoStockService;
    }

    @GetMapping("/api/restaurante/movimientos-stock")
    public List<MovimientoStockResponse> listar() {
        return movimientoStockService.listar().stream().map(MovimientoStockResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('PRODUCTO_STOCK_GESTIONAR')")
    @PostMapping("/api/restaurante/movimientos-stock")
    public MovimientoStockResponse registrar(@Valid @RequestBody MovimientoStockRequest request) {
        return MovimientoStockResponse.from(movimientoStockService.registrar(request));
    }

    @GetMapping("/api/restaurante/inventario")
    public List<InventarioItem> inventario() {
        return movimientoStockService.inventario();
    }
}
