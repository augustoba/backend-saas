package com.saasweb.restaurante.dto;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class ReporteDtos {

    private ReporteDtos() {
    }

    public record MontoPorDia(LocalDate fecha, BigDecimal total) {
    }

    public record MontoPorEtiqueta(String etiqueta, BigDecimal total) {
    }

    public record ReporteVentas(
            BigDecimal totalVentas, long cantidadOrdenes, BigDecimal ticketPromedio,
            List<MontoPorDia> ventasPorDia, List<MontoPorEtiqueta> ventasPorMedioPago
    ) {
    }

    public record ReportePropinas(BigDecimal totalPropinas, List<MontoPorDia> propinasPorDia) {
    }

    public record ReportePyL(
            BigDecimal ingresos, BigDecimal egresos, BigDecimal resultado, List<MontoPorEtiqueta> egresosPorCategoria
    ) {
    }

    public record ProductoVendido(Long productoId, String productoNombre, long cantidad, BigDecimal total) {
    }

    public record ReporteProductos(List<ProductoVendido> productos) {
    }
}
