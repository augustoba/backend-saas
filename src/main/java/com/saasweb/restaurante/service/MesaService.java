package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.MesaDtos.MesaRequest;
import com.saasweb.restaurante.dto.MesaDtos.SalonConMesas;
import com.saasweb.restaurante.dto.MesaDtos.SalonRequest;
import com.saasweb.restaurante.dto.MesaDtos.SalonResponse;
import com.saasweb.restaurante.dto.MesaDtos.MesaResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MesaService {

    private final SalonRepository salonRepository;
    private final MesaRepository mesaRepository;
    private final SucursalRepository sucursalRepository;

    public MesaService(SalonRepository salonRepository, MesaRepository mesaRepository,
                        SucursalRepository sucursalRepository) {
        this.salonRepository = salonRepository;
        this.mesaRepository = mesaRepository;
        this.sucursalRepository = sucursalRepository;
    }

    public List<SalonConMesas> listarSalonesConMesas() {
        return salonRepository.findByTenantIdOrderByOrdenAsc(tenantId()).stream()
                .map(salon -> new SalonConMesas(
                        SalonResponse.from(salon),
                        mesaRepository.findBySalonIdOrderByIdAsc(salon.getId()).stream().map(MesaResponse::from).toList()))
                .toList();
    }

    public Salon crearSalon(SalonRequest request) {
        Salon salon = new Salon();
        salon.setTenantId(tenantId());
        salon.setSucursalId(sucursalPrincipal());
        salon.setNombre(request.nombre());
        salon.setOrden(request.orden());
        return salonRepository.save(salon);
    }

    public Mesa crearMesa(MesaRequest request) {
        Salon salon = salonRepository.findByIdAndTenantId(request.salonId(), tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Salón no encontrado"));

        Mesa mesa = new Mesa();
        mesa.setSalon(salon);
        aplicar(mesa, request);
        return mesaRepository.save(mesa);
    }

    public Mesa actualizarMesa(Long id, MesaRequest request) {
        Mesa mesa = obtenerMesa(id);
        aplicar(mesa, request);
        return mesaRepository.save(mesa);
    }

    public Mesa cambiarEstado(Long id, Mesa.Estado estado) {
        Mesa mesa = obtenerMesa(id);
        mesa.setEstado(estado);
        return mesaRepository.save(mesa);
    }

    private Mesa obtenerMesa(Long id) {
        return mesaRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada"));
    }

    private void aplicar(Mesa mesa, MesaRequest request) {
        mesa.setNombre(request.nombre());
        if (request.forma() != null) mesa.setForma(request.forma());
        mesa.setPosX(request.posX());
        mesa.setPosY(request.posY());
        if (request.ancho() > 0) mesa.setAncho(request.ancho());
        if (request.alto() > 0) mesa.setAlto(request.alto());
        mesa.setRotacion(request.rotacion());
        if (request.capacidad() > 0) mesa.setCapacidad(request.capacidad());
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }

    /** Fase 1: una sola sucursal activa por tenant — se resuelve la primera hasta que haya selector en el front. */
    private Long sucursalPrincipal() {
        return sucursalRepository.findByTenantIdAndActivoTrue(tenantId()).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No hay sucursal activa para el tenant"))
                .getId();
    }
}
