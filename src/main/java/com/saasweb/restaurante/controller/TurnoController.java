package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.TurnoDtos.TurnoRequest;
import com.saasweb.restaurante.dto.TurnoDtos.TurnoResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurante/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @GetMapping
    public List<TurnoResponse> listar() {
        return turnoService.listar().stream().map(TurnoResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @PostMapping
    public TurnoResponse crear(@Valid @RequestBody TurnoRequest request) {
        return TurnoResponse.from(turnoService.crear(request));
    }

    @PreAuthorize("hasAuthority('ADMIN_CONFIGURAR')")
    @DeleteMapping("/{id}")
    public void archivar(@PathVariable Long id) {
        turnoService.archivar(id);
    }
}
