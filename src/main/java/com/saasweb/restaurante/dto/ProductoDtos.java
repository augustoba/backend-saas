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

public final class ProductoDtos {

    private ProductoDtos() {
    }

    public record CategoriaRequest(@NotBlank String nombre, int orden) {
    }

    public record CategoriaResponse(Long id, String nombre, int orden, boolean activo) {
        public static CategoriaResponse from(Categoria c) {
            return new CategoriaResponse(c.getId(), c.getNombre(), c.getOrden(), c.isActivo());
        }
    }

    public record ProductoRequest(
            @NotNull Long categoriaId,
            String codigo,
            @NotBlank String nombre,
            String descripcion,
            Producto.Tipo tipo,
            BigDecimal costo,
            BigDecimal precio,
            String unidad,
            boolean controlaStock,
            boolean disponibleOnline
    ) {
    }

    public record ProductoResponse(
            Long id, Long categoriaId, String categoriaNombre, String codigo, String nombre, String descripcion,
            Producto.Tipo tipo, BigDecimal costo, BigDecimal precio, String unidad, boolean disponible,
            boolean disponibleOnline, boolean activo, boolean controlaStock, Integer stockActual,
            List<Long> grupoModificadorIds
    ) {
        public static ProductoResponse from(Producto p) {
            return new ProductoResponse(p.getId(), p.getCategoria().getId(), p.getCategoria().getNombre(),
                    p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getTipo(), p.getCosto(), p.getPrecio(),
                    p.getUnidad(), p.isDisponible(), p.isDisponibleOnline(), p.isActivo(), p.isControlaStock(),
                    p.getStockActual(), p.getGruposModificadores().stream().map(GrupoModificador::getId).toList());
        }
    }

    public record DisponibilidadRequest(@NotNull Boolean disponible) {
    }
}
