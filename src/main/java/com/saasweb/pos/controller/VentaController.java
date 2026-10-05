package com.saasweb.pos.controller;

import com.saasweb.pos.dto.VentaDtos.*;
import com.saasweb.pos.service.CajaService;
import com.saasweb.pos.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Caja (turno) y ventas del mostrador. */
@RestController
@RequestMapping("/api/pos")
public class VentaController {

    private final CajaService cajas;
    private final VentaService ventas;

    public VentaController(CajaService cajas, VentaService ventas) {
        this.cajas = cajas;
        this.ventas = ventas;
    }

    // --- Caja ---

    /** La caja abierta con sus números; 204 si no hay ninguna abierta. */
    @GetMapping("/caja/actual")
    @PreAuthorize("hasAnyAuthority('CASH_REGISTER_VIEW','POS_USE')")
    public ResponseEntity<ResumenCajaResponse> actual() {
        return cajas.actual().map(c -> ResponseEntity.ok(cajas.resumen(c)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/caja/abrir")
    @PreAuthorize("hasAuthority('SHIFTS_MANAGE')")
    public ResponseEntity<ResumenCajaResponse> abrir(@Valid @RequestBody AbrirCajaRequest req, Authentication auth) {
        return ResponseEntity.status(201).body(cajas.resumen(cajas.abrir(req.montoInicial(), auth.getName())));
    }

    @PostMapping("/caja/movimientos")
    @PreAuthorize("hasAuthority('SHIFTS_MANAGE')")
    public ResponseEntity<MovimientoCajaResponse> movimiento(@Valid @RequestBody MovimientoCajaRequest req,
                                                             Authentication auth) {
        return ResponseEntity.status(201).body(MovimientoCajaResponse.from(
                cajas.movimiento(req.tipo(), req.monto(), req.motivo(), null, auth.getName())));
    }

    @PostMapping("/caja/cerrar")
    @PreAuthorize("hasAuthority('SHIFTS_MANAGE')")
    public ResumenCajaResponse cerrar(@Valid @RequestBody CerrarCajaRequest req, Authentication auth) {
        return cajas.cerrar(req.efectivoContado(), req.nota(), auth.getName());
    }

    @GetMapping("/cajas")
    @PreAuthorize("hasAuthority('CASH_REGISTER_VIEW')")
    public List<CajaResponse> historial(@RequestParam(defaultValue = "50") int limite) {
        return cajas.historial(limite).stream().map(CajaResponse::from).toList();
    }

    @GetMapping("/cajas/{id}")
    @PreAuthorize("hasAuthority('CASH_REGISTER_VIEW')")
    public ResumenCajaResponse caja(@PathVariable String id) {
        return cajas.resumen(cajas.get(id));
    }

    // --- Ventas ---

    @PostMapping("/ventas")
    @PreAuthorize("hasAuthority('POS_USE')")
    public ResponseEntity<VentaResponse> vender(@Valid @RequestBody VentaRequest req, Authentication auth) {
        return ResponseEntity.status(201).body(VentaResponse.from(ventas.vender(req, auth.getName())));
    }

    @GetMapping("/ventas")
    @PreAuthorize("hasAnyAuthority('POS_USE','CASH_REGISTER_VIEW')")
    public List<VentaResponse> listar(@RequestParam(required = false) String cajaId,
                                      @RequestParam(defaultValue = "100") int limite) {
        return ventas.listar(cajaId, limite).stream().map(VentaResponse::from).toList();
    }

    @GetMapping("/ventas/{id}")
    @PreAuthorize("hasAnyAuthority('POS_USE','CASH_REGISTER_VIEW')")
    public VentaResponse get(@PathVariable String id) {
        return VentaResponse.from(ventas.get(id));
    }

    @PostMapping("/ventas/{id}/anular")
    @PreAuthorize("hasAuthority('SHIFTS_MANAGE')")
    public VentaResponse anular(@PathVariable String id, @Valid @RequestBody AnularRequest req, Authentication auth) {
        return VentaResponse.from(ventas.anular(id, req.motivo(), auth.getName()));
    }

    @PostMapping("/ventas/{id}/reintentar-factura")
    @PreAuthorize("hasAuthority('POS_USE')")
    public VentaResponse reintentarFactura(@PathVariable String id) {
        return VentaResponse.from(ventas.reintentarFactura(id));
    }
}
