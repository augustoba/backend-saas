package com.saasweb.restaurante.dto;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public final class ProveedorDtos {

    private ProveedorDtos() {
    }

    public record ProveedorRequest(
            @NotBlank String nombre, String telefono, String email, String cuit, String direccion
    ) {
    }

    public record ProveedorResponse(
            Long id, String nombre, String telefono, String email, String cuit, String direccion,
            BigDecimal saldo, boolean activo
    ) {
        public static ProveedorResponse from(Proveedor p) {
            return new ProveedorResponse(p.getId(), p.getNombre(), p.getTelefono(), p.getEmail(), p.getCuit(),
                    p.getDireccion(), p.getSaldo(), p.isActivo());
        }
    }
}
