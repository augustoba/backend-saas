package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.TurnoDtos.TurnoRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final SucursalRepository sucursalRepository;

    public TurnoService(TurnoRepository turnoRepository, SucursalRepository sucursalRepository) {
        this.turnoRepository = turnoRepository;
        this.sucursalRepository = sucursalRepository;
    }

    public List<Turno> listar() {
        return turnoRepository.findByTenantIdOrderByHoraInicioAsc(tenantId());
    }

    public Turno crear(TurnoRequest request) {
        Turno turno = new Turno();
        turno.setTenantId(tenantId());
        turno.setSucursalId(sucursalPrincipal());
        aplicar(turno, request);
        return turnoRepository.save(turno);
    }

    public void archivar(Long id) {
        Turno turno = turnoRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));
        turno.setActivo(false);
        turnoRepository.save(turno);
    }

    private void aplicar(Turno turno, TurnoRequest request) {
        turno.setNombre(request.nombre());
        turno.setHoraInicio(request.horaInicio());
        turno.setHoraFin(request.horaFin());
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }

    private Long sucursalPrincipal() {
        return sucursalRepository.findByTenantIdAndActivoTrue(tenantId()).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No hay sucursal activa"))
                .getId();
    }
}
