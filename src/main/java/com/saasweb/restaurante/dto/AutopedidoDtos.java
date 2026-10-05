package com.saasweb.restaurante.dto;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class AutopedidoDtos {

    private AutopedidoDtos() {
    }

    public record ItemPedido(Long id, String productoNombre, int cantidad, BigDecimal subtotal, String notas, OrdenItem.Estado estado) {
        public static ItemPedido from(OrdenItem i) {
            return new ItemPedido(i.getId(), i.getProducto().getNombre(), i.getCantidad(), i.getSubtotal(),
                    i.getNotas(), i.getEstado());
        }
    }

    public record PedidoPublico(Long ordenId, String mesaNombre, Orden.Estado estado, BigDecimal total, List<ItemPedido> items) {
    }

    public record ProductoAutopedido(Long id, String nombre, String descripcion, BigDecimal precio) {
        public static ProductoAutopedido from(Producto p) {
            return new ProductoAutopedido(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio());
        }
    }

    public record SolicitudPendiente(Long id, Long ordenId, String mesaNombre, SolicitudMesa.Tipo tipo, Instant fecha) {
        public static SolicitudPendiente from(SolicitudMesa s) {
            return new SolicitudPendiente(s.getId(), s.getOrden().getId(),
                    s.getOrden().getMesa() != null ? s.getOrden().getMesa().getNombre() : null, s.getTipo(), s.getFecha());
        }
    }
}
