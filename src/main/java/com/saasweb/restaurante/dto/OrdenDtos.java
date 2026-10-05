package com.saasweb.restaurante.dto;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrdenDtos {

    private OrdenDtos() {
    }

    public record AbrirMesaRequest(@NotNull Long mesaId, Integer comensales, Long arqueoId) {
    }

    public record AbrirMostradorRequest(Long clienteId, Long arqueoId) {
    }

    public record AgregarItemRequest(@NotNull Long productoId, @Positive int cantidad, String notas) {
    }

    public record EditarItemRequest(@Positive int cantidad, String notas) {
    }

    public record RegistrarPagoRequest(@NotNull Long medioPagoId, @NotNull BigDecimal monto, BigDecimal propina, String referencia) {
    }

    public record DescuentoRequest(@NotNull @DecimalMin("0") BigDecimal monto) {
    }

    public record ItemCarritoRequest(@NotNull Long productoId, @Positive int cantidad, String notas) {
    }

    public record AbrirOnlineRequest(
            @jakarta.validation.constraints.NotBlank String nombreCliente,
            @jakarta.validation.constraints.NotBlank String telefonoCliente,
            String direccionEntrega,
            @NotNull Orden.Tipo tipo,
            String metodoPago,
            Long zonaEnvioId,
            @jakarta.validation.constraints.NotEmpty List<ItemCarritoRequest> items
    ) {
    }

    public record ItemResponse(
            Long id, Long productoId, String productoNombre, int cantidad, BigDecimal precioUnitario,
            BigDecimal subtotal, String notas, OrdenItem.Estado estado, int paso
    ) {
        public static ItemResponse from(OrdenItem i) {
            return new ItemResponse(i.getId(), i.getProducto().getId(), i.getProducto().getNombre(), i.getCantidad(),
                    i.getPrecioUnitario(), i.getSubtotal(), i.getNotas(), i.getEstado(), i.getPaso());
        }
    }

    public record PagoResponse(Long id, String medioPagoNombre, BigDecimal monto, BigDecimal propina, Instant fecha) {
        public static PagoResponse from(Pago p) {
            return new PagoResponse(p.getId(), p.getMedioPago().getNombre(), p.getMonto(), p.getPropina(), p.getFecha());
        }
    }

    public record OrdenResponse(
            Long id, Orden.Tipo tipo, Orden.Estado estado, Long mesaId, String mesaNombre, Integer comensales,
            String comentario, String usuarioNombre, BigDecimal subtotal, BigDecimal descuentoMonto,
            BigDecimal costoEnvio, BigDecimal propina, BigDecimal total, BigDecimal pagado, Instant fechaCreacion,
            List<ItemResponse> items, List<PagoResponse> pagos, String tokenSesion,
            String nombreClienteOnline, String telefonoClienteOnline, String direccionEntrega, int pasoActual,
            Orden.EstadoPreparacion estadoPreparacion, Instant fechaConfirmacion, Instant fechaTomado
    ) {
        public static OrdenResponse from(Orden o, List<OrdenItem> items, List<Pago> pagos) {
            BigDecimal pagado = pagos.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
            return new OrdenResponse(
                    o.getId(), o.getTipo(), o.getEstado(),
                    o.getMesa() != null ? o.getMesa().getId() : null,
                    o.getMesa() != null ? o.getMesa().getNombre() : null,
                    o.getComensales(), o.getComentario(),
                    o.getUsuario() != null
                            ? (o.getUsuario().getNombre() + " " + (o.getUsuario().getApellido() != null ? o.getUsuario().getApellido() : "")).trim()
                            : "Cliente (pedido online)",
                    o.getSubtotal(), o.getDescuentoMonto(), o.getCostoEnvio(), o.getPropina(), o.getTotal(), pagado,
                    o.getFechaCreacion(),
                    items.stream().map(ItemResponse::from).toList(),
                    pagos.stream().map(PagoResponse::from).toList(), o.getTokenSesion(),
                    o.getNombreClienteOnline(), o.getTelefonoClienteOnline(), o.getDireccionEntrega(), o.getPasoActual(),
                    o.getEstadoPreparacion(), o.getFechaConfirmacion(), o.getFechaTomado());
        }
    }

    public record OrdenResumen(Long id, Orden.Tipo tipo, Orden.Estado estado, String mesaNombre, BigDecimal total, Instant fechaCreacion) {
        public static OrdenResumen from(Orden o) {
            return new OrdenResumen(o.getId(), o.getTipo(), o.getEstado(),
                    o.getMesa() != null ? o.getMesa().getNombre() : null, o.getTotal(), o.getFechaCreacion());
        }
    }
}
