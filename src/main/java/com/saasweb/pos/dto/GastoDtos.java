package com.saasweb.pos.dto;

import com.saasweb.pos.model.Gasto;
import com.saasweb.pos.model.Proveedor;
import com.saasweb.pos.model.Venta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class GastoDtos {

    private GastoDtos() {}

    public record ProveedorRequest(@NotBlank @Size(max = 200) String nombre, @Size(max = 50) String telefono,
                                   @Size(max = 1000) String nota) {}

    public record ProveedorResponse(String id, String nombre, String telefono, String nota) {
        public static ProveedorResponse from(Proveedor p) {
            return new ProveedorResponse(p.getId(), p.getNombre(), p.getTelefono(), p.getNota());
        }
    }

    public record GastoRequest(
            LocalDate fecha,
            @NotBlank @Size(max = 200) String concepto,
            @Size(max = 100) String categoria,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal monto,
            String proveedorId,
            /** true = se pagó con el efectivo de la caja abierta (genera el egreso). */
            boolean pagadoDeCaja
    ) {}

    public record GastoResponse(String id, LocalDate fecha, String concepto, String categoria, BigDecimal monto,
                                String proveedorId, boolean pagadoDeCaja, String autorNombre) {
        public static GastoResponse from(Gasto g) {
            return new GastoResponse(g.getId(), g.getFecha(), g.getConcepto(), g.getCategoria(), g.getMonto(),
                    g.getProveedorId(), g.getMovimientoCajaId() != null, g.getAutorNombre());
        }
    }

    public record ArticuloVendido(String articuloId, String nombre, int unidades, BigDecimal total) {}

    /**
     * Resultado del período: lo vendido (sin anuladas), lo que costó esa
     * mercadería (costo al momento de vender; "sin costo" = artículos sin
     * costo cargado o importes sueltos), los gastos y lo que queda.
     */
    public record ReporteResponse(
            LocalDate desde, LocalDate hasta, int cantidadVentas, BigDecimal totalVendido,
            BigDecimal costoMercaderia, BigDecimal vendidoSinCosto, BigDecimal gananciaBruta,
            BigDecimal gastos, BigDecimal resultado, Map<Venta.MedioPago, BigDecimal> porMedioPago,
            Map<String, BigDecimal> gastosPorCategoria, List<ArticuloVendido> masVendidos
    ) {}
}
