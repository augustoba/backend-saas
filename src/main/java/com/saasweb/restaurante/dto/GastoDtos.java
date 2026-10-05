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
import java.time.LocalDate;

public final class GastoDtos {

    private GastoDtos() {
    }

    public record ClasificacionRequest(@NotBlank String nombre) {
    }

    public record ClasificacionResponse(Long id, String nombre, boolean activo) {
        public static ClasificacionResponse from(ClasificacionGasto c) {
            return new ClasificacionResponse(c.getId(), c.getNombre(), c.isActivo());
        }
    }

    public record CategoriaGastoRequest(@NotNull Long clasificacionId, @NotBlank String nombre) {
    }

    public record CategoriaGastoResponse(Long id, Long clasificacionId, String clasificacionNombre, String nombre, boolean activo) {
        public static CategoriaGastoResponse from(CategoriaGasto c) {
            return new CategoriaGastoResponse(c.getId(), c.getClasificacion().getId(),
                    c.getClasificacion().getNombre(), c.getNombre(), c.isActivo());
        }
    }

    public record GastoRequest(
            Long proveedorId,
            @NotNull Long categoriaGastoId,
            @NotNull LocalDate fecha,
            LocalDate fechaVencimiento,
            @NotNull BigDecimal monto,
            String descripcion,
            String numeroComprobante
    ) {
    }

    public record GastoResponse(
            Long id, String proveedorNombre, String categoriaNombre, LocalDate fecha,
            LocalDate fechaVencimiento, BigDecimal monto, String descripcion, Gasto.Estado estado
    ) {
        public static GastoResponse from(Gasto g) {
            return new GastoResponse(g.getId(), g.getProveedor() != null ? g.getProveedor().getNombre() : null,
                    g.getCategoriaGasto().getNombre(), g.getFecha(), g.getFechaVencimiento(), g.getMonto(),
                    g.getDescripcion(), g.getEstado());
        }
    }

    public record GastoKpis(long aVencer, long vencidos, BigDecimal totalAPagar, BigDecimal totalPagado) {
    }

    public record AsignarProveedorRequest(@NotNull Long proveedorId) {
    }
}
