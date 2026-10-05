package com.saasweb.restaurante.dto;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

public final class PreferenciasDtos {

    private PreferenciasDtos() {
    }

    public record PreferenciasRequest(
            boolean preciosConCentavos, boolean sonidosActivos, boolean pasosPorDefecto, boolean agruparPorProducto
    ) {
    }

    public record PreferenciasResponse(
            boolean preciosConCentavos, boolean sonidosActivos, boolean pasosPorDefecto, boolean agruparPorProducto
    ) {
        public static PreferenciasResponse from(Preferencias p) {
            return new PreferenciasResponse(p.isPreciosConCentavos(), p.isSonidosActivos(),
                    p.isPasosPorDefecto(), p.isAgruparPorProducto());
        }
    }
}
