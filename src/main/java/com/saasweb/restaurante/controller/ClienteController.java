package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ClienteDtos.ClienteKpis;
import com.saasweb.restaurante.dto.ClienteDtos.ClienteRequest;
import com.saasweb.restaurante.dto.ClienteDtos.ClienteResponse;
import com.saasweb.restaurante.dto.ClienteDtos.HabilitarCuentaCorrienteRequest;
import com.saasweb.restaurante.dto.ClienteDtos.MovimientoCuentaCorrienteRequest;
import com.saasweb.restaurante.dto.ClienteDtos.MovimientoCuentaCorrienteResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurante/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PreAuthorize("hasAuthority('CLIENTE_VER')")
    @GetMapping
    public PageResponse<ClienteResponse> buscar(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Cliente> resultado = clienteService.buscar(q, pageable);
        return PageResponse.of(resultado, resultado.getContent().stream().map(ClienteResponse::from).toList());
    }

    @PreAuthorize("hasAuthority('CLIENTE_VER')")
    @GetMapping("/kpis")
    public ClienteKpis kpis() {
        return clienteService.kpis();
    }

    @PreAuthorize("hasAuthority('CLIENTE_CREAR')")
    @PostMapping
    public ClienteResponse crear(@Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.from(clienteService.crear(request));
    }

    @PreAuthorize("hasAuthority('CLIENTE_CREAR')")
    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.from(clienteService.actualizar(id, request));
    }

    @PreAuthorize("hasAuthority('CLIENTE_CREAR')")
    @DeleteMapping("/{id}")
    public void archivar(@PathVariable Long id) {
        clienteService.archivar(id);
    }

    @PreAuthorize("hasAuthority('CUENTA_CORRIENTE_VER')")
    @PatchMapping("/{id}/cuenta-corriente/habilitar")
    public ClienteResponse habilitarCuentaCorriente(@PathVariable Long id, @Valid @RequestBody HabilitarCuentaCorrienteRequest request) {
        return ClienteResponse.from(clienteService.habilitarCuentaCorriente(id, request.habilitada()));
    }

    @PreAuthorize("hasAuthority('CUENTA_CORRIENTE_VER')")
    @GetMapping("/{id}/cuenta-corriente")
    public List<MovimientoCuentaCorrienteResponse> listarMovimientos(@PathVariable Long id) {
        return clienteService.listarMovimientos(id).stream().map(MovimientoCuentaCorrienteResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('CUENTA_CORRIENTE_VER')")
    @PostMapping("/{id}/cuenta-corriente")
    public MovimientoCuentaCorrienteResponse registrarMovimiento(@PathVariable Long id,
            @Valid @RequestBody MovimientoCuentaCorrienteRequest request) {
        return MovimientoCuentaCorrienteResponse.from(clienteService.registrarMovimiento(id, request));
    }
}
