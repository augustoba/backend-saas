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
import java.util.List;

public final class MesaDtos {

    private MesaDtos() {
    }

    public record SalonRequest(@NotBlank String nombre, int orden) {
    }

    public record SalonResponse(Long id, String nombre, int orden) {
        public static SalonResponse from(Salon s) {
            return new SalonResponse(s.getId(), s.getNombre(), s.getOrden());
        }
    }

    public record MesaRequest(
            @NotNull Long salonId,
            @NotBlank String nombre,
            Mesa.Forma forma,
            int posX,
            int posY,
            int ancho,
            int alto,
            int rotacion,
            int capacidad
    ) {
    }

    public record MesaEstadoRequest(@NotNull Mesa.Estado estado) {
    }

    public record MesaResponse(
            Long id,
            Long salonId,
            String nombre,
            Mesa.Forma forma,
            int posX,
            int posY,
            int ancho,
            int alto,
            int rotacion,
            Mesa.Estado estado,
            int capacidad
    ) {
        public static MesaResponse from(Mesa m) {
            return new MesaResponse(m.getId(), m.getSalon().getId(), m.getNombre(), m.getForma(),
                    m.getPosX(), m.getPosY(), m.getAncho(), m.getAlto(), m.getRotacion(),
                    m.getEstado(), m.getCapacidad());
        }
    }

    public record SalonConMesas(SalonResponse salon, List<MesaResponse> mesas) {
    }
}
