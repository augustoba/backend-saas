package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.SectorDtos.SectorRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SectorService {

    private final SectorRepository sectorRepository;

    public SectorService(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    public List<Sector> listar() {
        return sectorRepository.findByTenantIdOrderByNombreAsc(tenantId());
    }

    public Sector obtener(Long id) {
        return sectorRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado"));
    }

    public Sector crear(SectorRequest request) {
        Sector sector = new Sector();
        sector.setTenantId(tenantId());
        aplicar(sector, request);
        return sectorRepository.save(sector);
    }

    public Sector actualizar(Long id, SectorRequest request) {
        Sector sector = obtener(id);
        aplicar(sector, request);
        return sectorRepository.save(sector);
    }

    public void archivar(Long id) {
        Sector sector = obtener(id);
        sector.setActivo(false);
        sectorRepository.save(sector);
    }

    private void aplicar(Sector sector, SectorRequest request) {
        sector.setSucursalId(request.sucursalId());
        sector.setNombre(request.nombre());
        sector.setTipo(request.tipo());
        sector.setImpresoraIp(request.impresoraIp());
        sector.setKdsActivo(request.kdsActivo());
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
