package com.saasweb.restaurante.dto;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class ZonaEnvioDtos {

    private ZonaEnvioDtos() {
    }

    public record ZonaEnvioRequest(@NotBlank String nombre, @NotNull @DecimalMin("0") BigDecimal costo) {
    }

    public record ZonaEnvioResponse(Long id, String nombre, BigDecimal costo, boolean activo) {
        public static ZonaEnvioResponse from(ZonaEnvio z) {
            return new ZonaEnvioResponse(z.getId(), z.getNombre(), z.getCosto(), z.isActivo());
        }
    }

    public record ZonaEnvioPublica(Long id, String nombre, BigDecimal costo) {
        public static ZonaEnvioPublica from(ZonaEnvio z) {
            return new ZonaEnvioPublica(z.getId(), z.getNombre(), z.getCosto());
        }
    }
}
