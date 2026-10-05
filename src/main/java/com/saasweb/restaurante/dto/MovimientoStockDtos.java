package com.saasweb.restaurante.dto;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public final class MovimientoStockDtos {

    private MovimientoStockDtos() {
    }

    public record MovimientoStockRequest(
            @NotNull Long productoId,
            @NotNull MovimientoStock.Tipo tipo,
            @NotNull Integer cantidad,
            String motivo
    ) {
    }

    public record MovimientoStockResponse(
            Long id, Long productoId, String productoNombre, MovimientoStock.Tipo tipo,
            int cantidad, int stockResultante, String motivo, LocalDateTime fecha
    ) {
        public static MovimientoStockResponse from(MovimientoStock m) {
            return new MovimientoStockResponse(m.getId(), m.getProducto().getId(), m.getProducto().getNombre(),
                    m.getTipo(), m.getCantidad(), m.getStockResultante(), m.getMotivo(), m.getFecha());
        }
    }

    public record InventarioItem(Long productoId, String productoNombre, String categoriaNombre, Integer stockActual) {
    }
}
