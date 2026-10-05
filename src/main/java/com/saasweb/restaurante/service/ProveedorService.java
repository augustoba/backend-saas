package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ProveedorDtos.ProveedorRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public List<Proveedor> listar() {
        return proveedorRepository.findByTenantIdOrderByNombreAsc(tenantId());
    }

    public Proveedor crear(ProveedorRequest request) {
        Proveedor proveedor = new Proveedor();
        proveedor.setTenantId(tenantId());
        aplicar(proveedor, request);
        return proveedorRepository.save(proveedor);
    }

    public Proveedor actualizar(Long id, ProveedorRequest request) {
        Proveedor proveedor = obtener(id);
        aplicar(proveedor, request);
        return proveedorRepository.save(proveedor);
    }

    public void archivar(Long id) {
        Proveedor proveedor = obtener(id);
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
    }

    public void eliminar(Long id) {
        proveedorRepository.delete(obtener(id));
    }

    private Proveedor obtener(Long id) {
        return proveedorRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
    }

    private void aplicar(Proveedor proveedor, ProveedorRequest request) {
        proveedor.setNombre(request.nombre());
        proveedor.setTelefono(request.telefono());
        proveedor.setEmail(request.email());
        proveedor.setCuit(request.cuit());
        proveedor.setDireccion(request.direccion());
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
