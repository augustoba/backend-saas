package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.MovimientoStockDtos.InventarioItem;
import com.saasweb.restaurante.dto.MovimientoStockDtos.MovimientoStockRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public MovimientoStockService(MovimientoStockRepository movimientoRepository, ProductoRepository productoRepository) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
    }

    public List<MovimientoStock> listar() {
        return movimientoRepository.findByTenantIdOrderByFechaDesc(tenantId());
    }

    public List<InventarioItem> inventario() {
        return productoRepository.findByTenantIdOrderByNombreAsc(tenantId()).stream()
                .filter(Producto::isControlaStock)
                .map(p -> new InventarioItem(p.getId(), p.getNombre(), p.getCategoria().getNombre(), p.getStockActual()))
                .toList();
    }

    public MovimientoStock registrar(MovimientoStockRequest request) {
        Producto producto = productoRepository.findByIdAndTenantId(request.productoId(), tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        if (!producto.isControlaStock()) {
            throw new BadRequestException("El producto no controla stock");
        }

        int actual = producto.getStockActual() != null ? producto.getStockActual() : 0;
        int nuevo;
        switch (request.tipo()) {
            case INGRESO -> nuevo = actual + request.cantidad();
            case EGRESO -> nuevo = actual - request.cantidad();
            case AJUSTE -> nuevo = request.cantidad();
            default -> throw new BadRequestException("Tipo de movimiento inválido");
        }
        if (nuevo < 0) {
            throw new BadRequestException("El stock resultante no puede ser negativo");
        }

        producto.setStockActual(nuevo);
        productoRepository.save(producto);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setTenantId(tenantId());
        movimiento.setProducto(producto);
        movimiento.setTipo(request.tipo());
        movimiento.setCantidad(request.cantidad());
        movimiento.setStockResultante(nuevo);
        movimiento.setMotivo(request.motivo());
        return movimientoRepository.save(movimiento);
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
