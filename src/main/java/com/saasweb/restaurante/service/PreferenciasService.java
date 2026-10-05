package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.PreferenciasDtos.PreferenciasRequest;
import org.springframework.stereotype.Service;

@Service
public class PreferenciasService {

    private final PreferenciasRepository preferenciasRepository;
    private final SucursalRepository sucursalRepository;

    public PreferenciasService(PreferenciasRepository preferenciasRepository, SucursalRepository sucursalRepository) {
        this.preferenciasRepository = preferenciasRepository;
        this.sucursalRepository = sucursalRepository;
    }

    public Preferencias obtenerOCrear() {
        Long sucursalId = sucursalPrincipal();
        return preferenciasRepository.findBySucursalId(sucursalId).orElseGet(() -> {
            Preferencias preferencias = new Preferencias();
            preferencias.setSucursalId(sucursalId);
            return preferenciasRepository.save(preferencias);
        });
    }

    public Preferencias actualizar(PreferenciasRequest request) {
        Preferencias preferencias = obtenerOCrear();
        preferencias.setPreciosConCentavos(request.preciosConCentavos());
        preferencias.setSonidosActivos(request.sonidosActivos());
        preferencias.setPasosPorDefecto(request.pasosPorDefecto());
        preferencias.setAgruparPorProducto(request.agruparPorProducto());
        return preferenciasRepository.save(preferencias);
    }

    private Long sucursalPrincipal() {
        return sucursalRepository.findByTenantIdAndActivoTrue(TenantContext.getTenantId()).stream()
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("No hay sucursal activa")).getId();
    }
}
