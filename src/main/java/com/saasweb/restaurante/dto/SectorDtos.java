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

public final class SectorDtos {

    private SectorDtos() {
    }

    public record SectorRequest(
            @NotNull Long sucursalId,
            @NotBlank String nombre,
            @NotNull Sector.Tipo tipo,
            String impresoraIp,
            boolean kdsActivo
    ) {
    }

    public record SectorResponse(
            Long id,
            Long sucursalId,
            String nombre,
            Sector.Tipo tipo,
            String impresoraIp,
            boolean kdsActivo,
            boolean activo
    ) {
        public static SectorResponse from(Sector s) {
            return new SectorResponse(s.getId(), s.getSucursalId(), s.getNombre(), s.getTipo(),
                    s.getImpresoraIp(), s.isKdsActivo(), s.isActivo());
        }
    }
}
