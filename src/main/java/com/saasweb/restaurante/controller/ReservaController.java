package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ReservaDtos.CapacidadPorSalon;
import com.saasweb.restaurante.dto.ReservaDtos.ConfiguracionCapacidadRequest;
import com.saasweb.restaurante.dto.ReservaDtos.EstadoRequest;
import com.saasweb.restaurante.dto.ReservaDtos.MapaCalor;
import com.saasweb.restaurante.dto.ReservaDtos.ReservaRequest;
import com.saasweb.restaurante.dto.ReservaDtos.ReservaResponse;
import com.saasweb.restaurante.dto.ReservaDtos.TurnoReservaRequest;
import com.saasweb.restaurante.dto.ReservaDtos.TurnoReservaResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PreAuthorize("hasAuthority('RESERVA_VER')")
    @GetMapping("/api/restaurante/reservas")
    public List<ReservaResponse> listar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return reservaService.listarPorFecha(fecha).stream().map(ReservaResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('RESERVA_CREAR')")
    @PostMapping("/api/restaurante/reservas")
    public ReservaResponse crear(@Valid @RequestBody ReservaRequest request) {
        return ReservaResponse.from(reservaService.crear(request));
    }

    @PatchMapping("/api/restaurante/reservas/{id}/estado")
    public ReservaResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return ReservaResponse.from(reservaService.cambiarEstado(id, request.estado()));
    }

    @PreAuthorize("hasAuthority('RESERVA_VER')")
    @GetMapping("/api/restaurante/reservas/capacidad")
    public CapacidadPorSalon capacidad() {
        return reservaService.capacidadPorSalon();
    }

    @PreAuthorize("hasAuthority('RESERVA_CREAR')")
    @PutMapping("/api/restaurante/reservas/capacidad")
    public void actualizarCapacidad(@Valid @RequestBody ConfiguracionCapacidadRequest request) {
        reservaService.actualizarCapacidad(request);
    }

    @PreAuthorize("hasAuthority('RESERVA_VER')")
    @GetMapping("/api/restaurante/reservas/mapa-calor")
    public MapaCalor mapaCalor(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return reservaService.mapaCalor(fecha);
    }

    @GetMapping("/api/restaurante/turnos-reserva")
    public List<TurnoReservaResponse> listarTurnos() {
        return reservaService.listarTurnos().stream().map(TurnoReservaResponse::from).toList();
    }

    @PostMapping("/api/restaurante/turnos-reserva")
    public TurnoReservaResponse crearTurno(@Valid @RequestBody TurnoReservaRequest request) {
        return TurnoReservaResponse.from(reservaService.crearTurno(request));
    }

    @PutMapping("/api/restaurante/turnos-reserva/{id}")
    public TurnoReservaResponse actualizarTurno(@PathVariable Long id, @Valid @RequestBody TurnoReservaRequest request) {
        return TurnoReservaResponse.from(reservaService.actualizarTurno(id, request));
    }

    @DeleteMapping("/api/restaurante/turnos-reserva/{id}")
    public void archivarTurno(@PathVariable Long id) {
        reservaService.archivarTurno(id);
    }
}
