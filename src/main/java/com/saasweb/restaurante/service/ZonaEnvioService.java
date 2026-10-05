package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ZonaEnvioDtos.ZonaEnvioRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ZonaEnvioService {

    private final ZonaEnvioRepository zonaEnvioRepository;

    public ZonaEnvioService(ZonaEnvioRepository zonaEnvioRepository) {
        this.zonaEnvioRepository = zonaEnvioRepository;
    }

    public List<ZonaEnvio> listar() {
        return zonaEnvioRepository.findByTenantIdOrderByNombreAsc(tenantId());
    }

    public List<ZonaEnvio> listarActivas() {
        return zonaEnvioRepository.findByTenantIdAndActivoTrueOrderByNombreAsc(tenantId());
    }

    public ZonaEnvio crear(ZonaEnvioRequest request) {
        ZonaEnvio zona = new ZonaEnvio();
        zona.setTenantId(tenantId());
        aplicar(zona, request);
        return zonaEnvioRepository.save(zona);
    }

    public ZonaEnvio actualizar(Long id, ZonaEnvioRequest request) {
        ZonaEnvio zona = zonaEnvioRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Zona de envío no encontrada"));
        aplicar(zona, request);
        return zonaEnvioRepository.save(zona);
    }

    public void archivar(Long id) {
        ZonaEnvio zona = zonaEnvioRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Zona de envío no encontrada"));
        zona.setActivo(false);
        zonaEnvioRepository.save(zona);
    }

    private void aplicar(ZonaEnvio zona, ZonaEnvioRequest request) {
        zona.setNombre(request.nombre());
        zona.setCosto(request.costo());
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
