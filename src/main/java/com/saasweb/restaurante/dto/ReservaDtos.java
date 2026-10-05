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
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public final class ReservaDtos {

    private ReservaDtos() {
    }

    public record ReservaRequest(
            @NotNull Long salonId,
            @NotBlank String nombreCliente,
            String telefonoCliente,
            @NotNull LocalDate fecha,
            LocalTime hora,
            @Positive int cantidadPersonas,
            String notas
    ) {
    }

    public record ReservaResponse(
            Long id, Long salonId, String salonNombre, String nombreCliente, String telefonoCliente,
            LocalDate fecha, LocalTime hora, int cantidadPersonas, Reserva.Estado estado, String notas
    ) {
        public static ReservaResponse from(Reserva r) {
            return new ReservaResponse(r.getId(), r.getSalon().getId(), r.getSalon().getNombre(), r.getNombreCliente(),
                    r.getTelefonoCliente(), r.getFecha(), r.getHora(), r.getCantidadPersonas(), r.getEstado(), r.getNotas());
        }
    }

    public record EstadoRequest(@NotNull Reserva.Estado estado) {
    }

    public record CapacidadSalon(Long salonId, String salonNombre, int capacidadReservable) {
    }

    public record CapacidadPorSalon(int total, List<CapacidadSalon> porSalon) {
    }

    public record ConfiguracionCapacidadRequest(@NotNull Long salonId, int capacidadReservable) {
    }

    public record TurnoReservaRequest(
            @NotBlank String nombre,
            @NotNull LocalTime horaInicio,
            @NotNull LocalTime horaFin,
            @Positive int capacidadMaxima
    ) {
    }

    public record TurnoReservaResponse(
            Long id, String nombre, LocalTime horaInicio, LocalTime horaFin, int capacidadMaxima, boolean activo
    ) {
        public static TurnoReservaResponse from(TurnoReserva t) {
            return new TurnoReservaResponse(t.getId(), t.getNombre(), t.getHoraInicio(), t.getHoraFin(),
                    t.getCapacidadMaxima(), t.isActivo());
        }
    }

    public record FilaMapaCalor(Long salonId, String salonNombre, Map<Integer, Integer> personasPorHora) {
    }

    public record MapaCalor(LocalDate fecha, List<Integer> horas, List<FilaMapaCalor> filas) {
    }
}
