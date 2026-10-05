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
import java.time.LocalDate;

public final class ClienteDtos {

    private ClienteDtos() {
    }

    public record ClienteRequest(
            @NotBlank String nombre,
            String email,
            String telefono,
            String cuit,
            String instagram,
            String direccion,
            String notas,
            boolean tieneCuentaCorriente,
            BigDecimal limiteCredito,
            BigDecimal descuentoPorcentaje
    ) {
    }

    public record ClienteResponse(
            Long id, String nombre, String email, String telefono, String cuit, String instagram,
            boolean tieneCuentaCorriente, BigDecimal descuentoPorcentaje, boolean activo, BigDecimal saldoCuentaCorriente
    ) {
        public static ClienteResponse from(Cliente c) {
            return new ClienteResponse(c.getId(), c.getNombre(), c.getEmail(), c.getTelefono(), c.getCuit(),
                    c.getInstagram(), c.isTieneCuentaCorriente(), c.getDescuentoPorcentaje(), c.isActivo(),
                    c.getSaldoCuentaCorriente());
        }
    }

    public record ClienteKpis(long total, long conCuentaCorriente, long conDescuento, long activos) {
    }

    public record HabilitarCuentaCorrienteRequest(boolean habilitada) {
    }

    public record MovimientoCuentaCorrienteRequest(
            @NotNull CuentaCorrienteMovimiento.Tipo tipo,
            @NotNull BigDecimal monto,
            String descripcion
    ) {
    }

    public record MovimientoCuentaCorrienteResponse(
            Long id, CuentaCorrienteMovimiento.Tipo tipo, BigDecimal monto, String descripcion,
            BigDecimal saldoResultante, LocalDate fecha
    ) {
        public static MovimientoCuentaCorrienteResponse from(CuentaCorrienteMovimiento m) {
            return new MovimientoCuentaCorrienteResponse(m.getId(), m.getTipo(), m.getMonto(), m.getDescripcion(),
                    m.getSaldoResultante(), m.getFecha());
        }
    }
}
