package com.saasweb.restaurante.dto;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import java.time.Instant;
import java.util.List;

public final class KdsDtos {

    private KdsDtos() {
    }

    public record KdsItem(Long itemId, Long productoId, String productoNombre, int cantidad, String notas, OrdenItem.Estado estado) {
        public static KdsItem from(OrdenItem i) {
            return new KdsItem(i.getId(), i.getProducto().getId(), i.getProducto().getNombre(), i.getCantidad(),
                    i.getNotas(), i.getEstado());
        }
    }

    public record KdsOrden(Long ordenId, Orden.Tipo tipo, String mesaNombre, Instant fechaCreacion, List<KdsItem> items) {
    }
}
