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
import java.time.Instant;

public final class CajaDtos {

    private CajaDtos() {
    }

    public record CajaRequest(@NotBlank String nombre, boolean activa) {
    }

    public record CajaResponse(Long id, String nombre, boolean activo) {
        public static CajaResponse from(Caja c) {
            return new CajaResponse(c.getId(), c.getNombre(), c.isActivo());
        }
    }

    public record AbrirArqueoRequest(@NotNull Long cajaId, @NotNull BigDecimal montoApertura) {
    }

    public record CerrarArqueoRequest(@NotNull BigDecimal montoCierreDeclarado, String observaciones) {
    }

    public record ArqueoResponse(
            Long id, String cajaNombre, String usuarioAperturaNombre, Instant fechaApertura, Instant fechaCierre,
            BigDecimal montoApertura, BigDecimal montoCierreDeclarado, BigDecimal montoCierreSistema,
            BigDecimal diferencia, Arqueo.Estado estado, BigDecimal totalVentas, BigDecimal efectivoEnCaja
    ) {
    }

    public record MovimientoRequest(
            @NotNull Long arqueoId, @NotNull MovimientoCaja.Tipo tipo, @NotNull BigDecimal monto,
            String descripcion, Long medioPagoId
    ) {
    }

    public record MovimientoResponse(
            Long id, MovimientoCaja.Tipo tipo, BigDecimal monto, String descripcion, Instant fecha
    ) {
        public static MovimientoResponse from(MovimientoCaja m) {
            return new MovimientoResponse(m.getId(), m.getTipo(), m.getMonto(), m.getDescripcion(), m.getFecha());
        }
    }
}
