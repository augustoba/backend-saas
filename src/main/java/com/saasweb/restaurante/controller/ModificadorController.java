package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ModificadorDtos.AsignarGruposRequest;
import com.saasweb.restaurante.dto.ModificadorDtos.GrupoModificadorRequest;
import com.saasweb.restaurante.dto.ModificadorDtos.GrupoModificadorResponse;
import com.saasweb.restaurante.dto.ModificadorDtos.ModificadorRequest;
import com.saasweb.restaurante.dto.ModificadorDtos.ModificadorResponse;
import com.saasweb.restaurante.dto.ProductoDtos.ProductoResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModificadorController {

    private final ModificadorService modificadorService;

    public ModificadorController(ModificadorService modificadorService) {
        this.modificadorService = modificadorService;
    }

    @GetMapping("/api/restaurante/grupos-modificadores")
    public List<GrupoModificadorResponse> listarGrupos() {
        return modificadorService.listarGrupos().stream()
                .map(g -> GrupoModificadorResponse.from(g, modificadorService.listarModificadores(g.getId())))
                .toList();
    }

    @PreAuthorize("hasAuthority('PRODUCTO_MODIFICADORES_GESTIONAR')")
    @PostMapping("/api/restaurante/grupos-modificadores")
    public GrupoModificadorResponse crearGrupo(@Valid @RequestBody GrupoModificadorRequest request) {
        GrupoModificador grupo = modificadorService.crearGrupo(request);
        return GrupoModificadorResponse.from(grupo, List.of());
    }

    @PreAuthorize("hasAuthority('PRODUCTO_MODIFICADORES_GESTIONAR')")
    @PutMapping("/api/restaurante/grupos-modificadores/{id}")
    public GrupoModificadorResponse actualizarGrupo(@PathVariable Long id, @Valid @RequestBody GrupoModificadorRequest request) {
        GrupoModificador grupo = modificadorService.actualizarGrupo(id, request);
        return GrupoModificadorResponse.from(grupo, modificadorService.listarModificadores(id));
    }

    @PreAuthorize("hasAuthority('PRODUCTO_MODIFICADORES_GESTIONAR')")
    @DeleteMapping("/api/restaurante/grupos-modificadores/{id}")
    public void archivarGrupo(@PathVariable Long id) {
        modificadorService.archivarGrupo(id);
    }

    @PreAuthorize("hasAuthority('PRODUCTO_MODIFICADORES_GESTIONAR')")
    @PostMapping("/api/restaurante/grupos-modificadores/{grupoId}/modificadores")
    public ModificadorResponse agregarModificador(@PathVariable Long grupoId, @Valid @RequestBody ModificadorRequest request) {
        return ModificadorResponse.from(modificadorService.agregarModificador(grupoId, request));
    }

    @PreAuthorize("hasAuthority('PRODUCTO_MODIFICADORES_GESTIONAR')")
    @PutMapping("/api/restaurante/grupos-modificadores/{grupoId}/modificadores/{id}")
    public ModificadorResponse actualizarModificador(@PathVariable Long grupoId, @PathVariable Long id,
                                                       @Valid @RequestBody ModificadorRequest request) {
        return ModificadorResponse.from(modificadorService.actualizarModificador(grupoId, id, request));
    }

    @PreAuthorize("hasAuthority('PRODUCTO_MODIFICADORES_GESTIONAR')")
    @DeleteMapping("/api/restaurante/grupos-modificadores/{grupoId}/modificadores/{id}")
    public void eliminarModificador(@PathVariable Long grupoId, @PathVariable Long id) {
        modificadorService.eliminarModificador(grupoId, id);
    }

    @PreAuthorize("hasAuthority('PRODUCTO_MODIFICADORES_GESTIONAR')")
    @PutMapping("/api/restaurante/productos/{productoId}/grupos-modificadores")
    public ProductoResponse asignarGrupos(@PathVariable Long productoId, @Valid @RequestBody AsignarGruposRequest request) {
        return ProductoResponse.from(modificadorService.asignarGrupos(productoId, request.grupoIds()));
    }
}
