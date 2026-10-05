package com.saasweb.restaurante.dto;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public final class MedioPagoDtos {

    private MedioPagoDtos() {
    }

    public record MedioPagoRequest(
            @NotBlank String nombre,
            boolean esEfectivo,
            boolean esCuentaCorriente,
            boolean requiereReferencia
    ) {
    }

    public record ReordenarRequest(@NotEmpty List<Long> ids) {
    }

    public record MedioPagoResponse(
            Long id, String nombre, boolean esEfectivo, boolean esCuentaCorriente,
            boolean requiereReferencia, boolean activo, int orden
    ) {
        public static MedioPagoResponse from(MedioPago m) {
            return new MedioPagoResponse(m.getId(), m.getNombre(), m.isEsEfectivo(), m.isEsCuentaCorriente(),
                    m.isRequiereReferencia(), m.isActivo(), m.getOrden());
        }
    }
}
