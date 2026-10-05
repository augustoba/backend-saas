package com.saasweb.pos.dto;

import com.saasweb.core.model.Comprobante;
import com.saasweb.pos.model.Caja;
import com.saasweb.pos.model.MovimientoCaja;
import com.saasweb.pos.model.Venta;
import com.saasweb.pos.model.VentaLinea;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class VentaDtos {

    private VentaDtos() {}

    // --- Ventas ---

    /** {@code articuloId} null = "varios": van {@code nombre} y {@code precio} sueltos. */
    public record ItemRequest(
            String articuloId,
            @Min(1) int cantidad,
            @Size(max = 200) String nombre,
            @DecimalMin("0") BigDecimal precio
    ) {}

    public record VentaRequest(
            @NotEmpty List<@Valid ItemRequest> items,
            @NotNull Venta.MedioPago medioPago,
            @DecimalMin("0") BigDecimal montoEntregado,
            @Size(max = 200) String referencia,
            @Pattern(regexp = "^$|^\\d{11}$", message = "El CUIT tiene 11 números") String cuitComprador
    ) {}

    public record AnularRequest(@Size(max = 300) String motivo) {}

    public record LineaResponse(String articuloId, String nombre, BigDecimal precioUnitario, int cantidad,
                                BigDecimal iva, BigDecimal subtotal) {
        static LineaResponse from(VentaLinea l) {
            return new LineaResponse(l.getArticuloId(), l.getNombre(), l.getPrecioUnitario(), l.getCantidad(),
                    l.getIva(), l.subtotal());
        }
    }

    /** La factura vista desde la venta: aprobada (CAE) o el error del último intento. */
    public record FacturaResponse(String tipo, String cae, String caeVencimiento, Long numero, Integer puntoVenta,
                                  String qrUrl, String error) {
        static FacturaResponse from(Comprobante c) {
            if (c == null) return null;
            return new FacturaResponse(c.getTipo(), c.getCae(), c.getCaeVencimiento(), c.getNumero(),
                    c.getPuntoVenta(), c.getQrUrl(), c.getError());
        }
    }

    public record VentaResponse(
            String id, String codigo, int numero, String cajaId, Instant fecha, String cajeroNombre,
            BigDecimal total, Venta.MedioPago medioPago, BigDecimal montoEntregado, BigDecimal vuelto,
            String referencia, String cuitComprador, Venta.Estado estado, Instant anuladaEn,
            String anuladaPorNombre, String motivoAnulacion, FacturaResponse factura, List<LineaResponse> lineas
    ) {
        public static VentaResponse from(Venta v) {
            return new VentaResponse(v.getId(), v.codigo(), v.getNumero(), v.getCajaId(), v.getFecha(),
                    v.getCajeroNombre(), v.getTotal(), v.getMedioPago(), v.getMontoEntregado(), v.getVuelto(),
                    v.getReferencia(), v.getCuitComprador(), v.getEstado(), v.getAnuladaEn(), v.getAnuladaPorNombre(),
                    v.getMotivoAnulacion(), FacturaResponse.from(v.getFactura()),
                    v.getLineas().stream().map(LineaResponse::from).toList());
        }
    }

    // --- Caja ---

    public record AbrirCajaRequest(@NotNull @DecimalMin("0") BigDecimal montoInicial) {}

    public record MovimientoCajaRequest(
            @NotNull MovimientoCaja.Tipo tipo,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal monto,
            @NotBlank @Size(max = 300) String motivo
    ) {}

    public record CerrarCajaRequest(@NotNull @DecimalMin("0") BigDecimal efectivoContado, @Size(max = 500) String nota) {}

    public record MovimientoCajaResponse(String id, MovimientoCaja.Tipo tipo, BigDecimal monto, String motivo,
                                         String gastoId, String autorNombre, Instant fecha) {
        public static MovimientoCajaResponse from(MovimientoCaja m) {
            return new MovimientoCajaResponse(m.getId(), m.getTipo(), m.getMonto(), m.getMotivo(), m.getGastoId(),
                    m.getAutorNombre(), m.getFecha());
        }
    }

    public record CajaResponse(
            String id, int numero, Caja.Estado estado, Instant abiertaEn, String abiertaPorNombre,
            BigDecimal montoInicial, Instant cerradaEn, String cerradaPorNombre, BigDecimal efectivoEsperado,
            BigDecimal efectivoContado, BigDecimal diferencia, String notaCierre
    ) {
        public static CajaResponse from(Caja c) {
            return new CajaResponse(c.getId(), c.getNumero(), c.getEstado(), c.getAbiertaEn(), c.getAbiertaPorNombre(),
                    c.getMontoInicial(), c.getCerradaEn(), c.getCerradaPorNombre(), c.getEfectivoEsperado(),
                    c.getEfectivoContado(), c.getDiferencia(), c.getNotaCierre());
        }
    }

    /**
     * La caja con sus números: lo vendido por medio de pago (sólo ventas
     * completadas), ingresos/egresos y el efectivo que debería haber.
     */
    public record ResumenCajaResponse(
            CajaResponse caja,
            int cantidadVentas,
            int ventasAnuladas,
            BigDecimal totalVendido,
            Map<Venta.MedioPago, BigDecimal> porMedioPago,
            BigDecimal ingresos,
            BigDecimal egresos,
            BigDecimal efectivoEsperado,
            List<MovimientoCajaResponse> movimientos
    ) {}
}
