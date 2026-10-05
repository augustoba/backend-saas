package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ReservaDtos.CapacidadPorSalon;
import com.saasweb.restaurante.dto.ReservaDtos.CapacidadSalon;
import com.saasweb.restaurante.dto.ReservaDtos.ConfiguracionCapacidadRequest;
import com.saasweb.restaurante.dto.ReservaDtos.FilaMapaCalor;
import com.saasweb.restaurante.dto.ReservaDtos.MapaCalor;
import com.saasweb.restaurante.dto.ReservaDtos.ReservaRequest;
import com.saasweb.restaurante.dto.ReservaDtos.TurnoReservaRequest;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {

    private static final int HORA_DESDE = 11;
    private static final int HORA_HASTA = 23;

    private final ReservaRepository reservaRepository;
    private final SalonRepository salonRepository;
    private final ConfiguracionReservaSalonRepository configRepository;
    private final TurnoReservaRepository turnoReservaRepository;

    public ReservaService(ReservaRepository reservaRepository, SalonRepository salonRepository,
                           ConfiguracionReservaSalonRepository configRepository,
                           TurnoReservaRepository turnoReservaRepository) {
        this.reservaRepository = reservaRepository;
        this.salonRepository = salonRepository;
        this.configRepository = configRepository;
        this.turnoReservaRepository = turnoReservaRepository;
    }

    public List<Reserva> listarPorFecha(LocalDate fecha) {
        return reservaRepository.findByTenantIdAndFechaOrderByHoraAsc(tenantId(), fecha);
    }

    public Reserva crear(ReservaRequest request) {
        Salon salon = salonRepository.findByIdAndTenantId(request.salonId(), tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Salón no encontrado"));

        Reserva reserva = new Reserva();
        reserva.setTenantId(tenantId());
        reserva.setSucursalId(salon.getSucursalId());
        reserva.setSalon(salon);
        reserva.setNombreCliente(request.nombreCliente());
        reserva.setTelefonoCliente(request.telefonoCliente());
        reserva.setFecha(request.fecha());
        reserva.setHora(request.hora());
        reserva.setCantidadPersonas(request.cantidadPersonas());
        reserva.setNotas(request.notas());
        return reservaRepository.save(reserva);
    }

    public Reserva cambiarEstado(Long id, Reserva.Estado estado) {
        Reserva reserva = reservaRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        reserva.setEstado(estado);
        return reservaRepository.save(reserva);
    }

    public CapacidadPorSalon capacidadPorSalon() {
        List<CapacidadSalon> lista = salonRepository.findByTenantIdOrderByOrdenAsc(tenantId()).stream()
                .map(salon -> new CapacidadSalon(salon.getId(), salon.getNombre(),
                        configRepository.findBySalonId(salon.getId()).map(ConfiguracionReservaSalon::getCapacidadReservable).orElse(0)))
                .toList();
        int total = lista.stream().mapToInt(CapacidadSalon::capacidadReservable).sum();
        return new CapacidadPorSalon(total, lista);
    }

    public void actualizarCapacidad(ConfiguracionCapacidadRequest request) {
        ConfiguracionReservaSalon config = configRepository.findBySalonId(request.salonId())
                .orElseGet(() -> {
                    ConfiguracionReservaSalon nuevo = new ConfiguracionReservaSalon();
                    nuevo.setSalonId(request.salonId());
                    return nuevo;
                });
        config.setCapacidadReservable(request.capacidadReservable());
        configRepository.save(config);
    }

    public MapaCalor mapaCalor(LocalDate fecha) {
        List<Reserva> reservas = listarPorFecha(fecha);
        List<Integer> horas = new java.util.ArrayList<>();
        for (int h = HORA_DESDE; h <= HORA_HASTA; h++) horas.add(h);

        List<Salon> salones = salonRepository.findByTenantIdOrderByOrdenAsc(tenantId());
        List<FilaMapaCalor> filas = salones.stream().map(salon -> {
            Map<Integer, Integer> personasPorHora = new LinkedHashMap<>();
            for (int h : horas) personasPorHora.put(h, 0);
            reservas.stream()
                    .filter(r -> r.getSalon().getId().equals(salon.getId()))
                    .filter(r -> r.getHora() != null)
                    .filter(r -> r.getEstado() != Reserva.Estado.CANCELADA)
                    .forEach(r -> {
                        int hora = r.getHora().getHour();
                        if (personasPorHora.containsKey(hora)) {
                            personasPorHora.merge(hora, r.getCantidadPersonas(), Integer::sum);
                        }
                    });
            return new FilaMapaCalor(salon.getId(), salon.getNombre(), personasPorHora);
        }).toList();

        return new MapaCalor(fecha, horas, filas);
    }

    // --- Turnos de reserva ---
    public List<TurnoReserva> listarTurnos() {
        return turnoReservaRepository.findByTenantIdOrderByHoraInicioAsc(tenantId());
    }

    public TurnoReserva crearTurno(TurnoReservaRequest request) {
        TurnoReserva turno = new TurnoReserva();
        turno.setTenantId(tenantId());
        aplicar(turno, request);
        return turnoReservaRepository.save(turno);
    }

    public TurnoReserva actualizarTurno(Long id, TurnoReservaRequest request) {
        TurnoReserva turno = turnoReservaRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno de reserva no encontrado"));
        aplicar(turno, request);
        return turnoReservaRepository.save(turno);
    }

    public void archivarTurno(Long id) {
        TurnoReserva turno = turnoReservaRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno de reserva no encontrado"));
        turno.setActivo(false);
        turnoReservaRepository.save(turno);
    }

    private void aplicar(TurnoReserva turno, TurnoReservaRequest request) {
        turno.setNombre(request.nombre());
        turno.setHoraInicio(request.horaInicio());
        turno.setHoraFin(request.horaFin());
        turno.setCapacidadMaxima(request.capacidadMaxima());
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
