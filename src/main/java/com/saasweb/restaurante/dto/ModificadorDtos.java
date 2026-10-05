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
import java.math.BigDecimal;
import java.util.List;

public final class ModificadorDtos {

    private ModificadorDtos() {
    }

    public record GrupoModificadorRequest(
            @NotBlank String nombre,
            boolean obligatorio,
            boolean multiple,
            Integer min,
            Integer max
    ) {
    }

    public record ModificadorRequest(@NotBlank String nombre, BigDecimal precioAdicional) {
    }

    public record ModificadorResponse(Long id, String nombre, BigDecimal precioAdicional, boolean activo) {
        public static ModificadorResponse from(Modificador m) {
            return new ModificadorResponse(m.getId(), m.getNombre(), m.getPrecioAdicional(), m.isActivo());
        }
    }

    public record GrupoModificadorResponse(
            Long id, String nombre, boolean obligatorio, boolean multiple, Integer min, Integer max,
            boolean activo, List<ModificadorResponse> modificadores
    ) {
        public static GrupoModificadorResponse from(GrupoModificador g, List<Modificador> modificadores) {
            return new GrupoModificadorResponse(g.getId(), g.getNombre(), g.isObligatorio(), g.isMultiple(),
                    g.getMin(), g.getMax(), g.isActivo(), modificadores.stream().map(ModificadorResponse::from).toList());
        }
    }

    public record AsignarGruposRequest(@NotNull List<Long> grupoIds) {
    }
}
