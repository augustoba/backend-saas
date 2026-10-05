package com.saasweb.pos.controller;

import com.saasweb.pos.dto.GastoDtos.*;
import com.saasweb.pos.service.GastoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** Gastos, proveedores y reporte de resultados del punto de venta. */
@RestController
@RequestMapping("/api/pos")
public class GastoController {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final GastoService service;

    public GastoController(GastoService service) {
        this.service = service;
    }

    @GetMapping("/proveedores")
    @PreAuthorize("hasAnyAuthority('SUPPLIERS_MANAGE','EXPENSES_MANAGE')")
    public List<ProveedorResponse> proveedores() {
        return service.proveedores().stream().map(ProveedorResponse::from).toList();
    }

    @PostMapping("/proveedores")
    @PreAuthorize("hasAuthority('SUPPLIERS_MANAGE')")
    public ResponseEntity<ProveedorResponse> crearProveedor(@Valid @RequestBody ProveedorRequest req) {
        return ResponseEntity.status(201).body(ProveedorResponse.from(service.guardarProveedor(null, req)));
    }

    @PutMapping("/proveedores/{id}")
    @PreAuthorize("hasAuthority('SUPPLIERS_MANAGE')")
    public ProveedorResponse actualizarProveedor(@PathVariable String id, @Valid @RequestBody ProveedorRequest req) {
        return ProveedorResponse.from(service.guardarProveedor(id, req));
    }

    @DeleteMapping("/proveedores/{id}")
    @PreAuthorize("hasAuthority('SUPPLIERS_MANAGE')")
    public ResponseEntity<Void> borrarProveedor(@PathVariable String id) {
        service.borrarProveedor(id);
        return ResponseEntity.noContent().build();
    }

    /** Sin fechas: el mes en curso. */
    @GetMapping("/gastos")
    @PreAuthorize("hasAuthority('EXPENSES_MANAGE')")
    public List<GastoResponse> gastos(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate hoy = LocalDate.now(ZONA);
        return service.gastos(desde != null ? desde : hoy.withDayOfMonth(1), hasta != null ? hasta : hoy)
                .stream().map(GastoResponse::from).toList();
    }

    @PostMapping("/gastos")
    @PreAuthorize("hasAuthority('EXPENSES_MANAGE')")
    public ResponseEntity<GastoResponse> crearGasto(@Valid @RequestBody GastoRequest req, Authentication auth) {
        return ResponseEntity.status(201).body(GastoResponse.from(service.crearGasto(req, auth.getName())));
    }

    @DeleteMapping("/gastos/{id}")
    @PreAuthorize("hasAuthority('EXPENSES_MANAGE')")
    public ResponseEntity<Void> borrarGasto(@PathVariable String id) {
        service.borrarGasto(id);
        return ResponseEntity.noContent().build();
    }

    /** Resultado del período (sin fechas: el mes en curso). */
    @GetMapping("/reporte")
    @PreAuthorize("hasAuthority('FINANCE_VIEW')")
    public ReporteResponse reporte(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate hoy = LocalDate.now(ZONA);
        return service.reporte(desde != null ? desde : hoy.withDayOfMonth(1), hasta != null ? hasta : hoy);
    }
}
