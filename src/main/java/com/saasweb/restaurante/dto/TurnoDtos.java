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
import java.time.LocalTime;

public final class TurnoDtos {

    private TurnoDtos() {
    }

    public record TurnoRequest(@NotBlank String nombre, @NotNull LocalTime horaInicio, @NotNull LocalTime horaFin) {
    }

    public record TurnoResponse(Long id, String nombre, LocalTime horaInicio, LocalTime horaFin, boolean activo) {
        public static TurnoResponse from(Turno t) {
            return new TurnoResponse(t.getId(), t.getNombre(), t.getHoraInicio(), t.getHoraFin(), t.isActivo());
        }
    }
}
