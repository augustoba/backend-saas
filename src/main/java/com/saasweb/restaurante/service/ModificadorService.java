package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ModificadorDtos.GrupoModificadorRequest;
import com.saasweb.restaurante.dto.ModificadorDtos.ModificadorRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ModificadorService {

    private final GrupoModificadorRepository grupoRepository;
    private final ModificadorRepository modificadorRepository;
    private final ProductoRepository productoRepository;

    public ModificadorService(GrupoModificadorRepository grupoRepository, ModificadorRepository modificadorRepository,
                               ProductoRepository productoRepository) {
        this.grupoRepository = grupoRepository;
        this.modificadorRepository = modificadorRepository;
        this.productoRepository = productoRepository;
    }

    public List<GrupoModificador> listarGrupos() {
        return grupoRepository.findByTenantIdOrderByNombreAsc(tenantId());
    }

    public List<Modificador> listarModificadores(Long grupoId) {
        obtenerGrupo(grupoId);
        return modificadorRepository.findByGrupoModificadorIdOrderByNombreAsc(grupoId);
    }

    public GrupoModificador crearGrupo(GrupoModificadorRequest request) {
        GrupoModificador grupo = new GrupoModificador();
        grupo.setTenantId(tenantId());
        aplicar(grupo, request);
        return grupoRepository.save(grupo);
    }

    public GrupoModificador actualizarGrupo(Long id, GrupoModificadorRequest request) {
        GrupoModificador grupo = obtenerGrupo(id);
        aplicar(grupo, request);
        return grupoRepository.save(grupo);
    }

    public void archivarGrupo(Long id) {
        GrupoModificador grupo = obtenerGrupo(id);
        grupo.setActivo(false);
        grupoRepository.save(grupo);
    }

    public Modificador agregarModificador(Long grupoId, ModificadorRequest request) {
        GrupoModificador grupo = obtenerGrupo(grupoId);
        Modificador modificador = new Modificador();
        modificador.setGrupoModificador(grupo);
        aplicar(modificador, request);
        return modificadorRepository.save(modificador);
    }

    public Modificador actualizarModificador(Long grupoId, Long id, ModificadorRequest request) {
        obtenerGrupo(grupoId);
        Modificador modificador = modificadorRepository.findByIdAndGrupoModificadorId(id, grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Modificador no encontrado"));
        aplicar(modificador, request);
        return modificadorRepository.save(modificador);
    }

    public void eliminarModificador(Long grupoId, Long id) {
        obtenerGrupo(grupoId);
        Modificador modificador = modificadorRepository.findByIdAndGrupoModificadorId(id, grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Modificador no encontrado"));
        modificador.setActivo(false);
        modificadorRepository.save(modificador);
    }

    public Producto asignarGrupos(Long productoId, List<Long> grupoIds) {
        Producto producto = productoRepository.findByIdAndTenantId(productoId, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        Set<GrupoModificador> grupos = new HashSet<>();
        for (Long grupoId : grupoIds) {
            grupos.add(obtenerGrupo(grupoId));
        }
        producto.setGruposModificadores(grupos);
        return productoRepository.save(producto);
    }

    private GrupoModificador obtenerGrupo(Long id) {
        return grupoRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de modificadores no encontrado"));
    }

    private void aplicar(GrupoModificador grupo, GrupoModificadorRequest request) {
        grupo.setNombre(request.nombre());
        grupo.setObligatorio(request.obligatorio());
        grupo.setMultiple(request.multiple());
        grupo.setMin(request.min());
        grupo.setMax(request.max());
    }

    private void aplicar(Modificador modificador, ModificadorRequest request) {
        modificador.setNombre(request.nombre());
        if (request.precioAdicional() != null) modificador.setPrecioAdicional(request.precioAdicional());
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
