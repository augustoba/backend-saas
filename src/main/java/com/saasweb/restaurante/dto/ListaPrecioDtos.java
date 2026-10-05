package com.saasweb.restaurante.dto;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public final class ListaPrecioDtos {

    private ListaPrecioDtos() {
    }

    public record ListaPrecioRequest(@NotBlank String nombre, String descripcion) {
    }

    public record ListaPrecioResponse(Long id, String nombre, String descripcion, boolean activo) {
        public static ListaPrecioResponse from(ListaPrecio l) {
            return new ListaPrecioResponse(l.getId(), l.getNombre(), l.getDescripcion(), l.isActivo());
        }
    }

    public record ItemRequest(@NotNull Long productoId, @NotNull BigDecimal precio) {
    }

    public record ItemResponse(Long id, Long productoId, String productoNombre, BigDecimal precioBase, BigDecimal precio) {
        public static ItemResponse from(ListaPrecioItem item) {
            return new ItemResponse(item.getId(), item.getProducto().getId(), item.getProducto().getNombre(),
                    item.getProducto().getPrecio(), item.getPrecio());
        }
    }
}
