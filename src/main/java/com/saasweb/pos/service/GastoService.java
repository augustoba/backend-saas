package com.saasweb.pos.service;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;
import com.saasweb.pos.dto.GastoDtos.*;
import com.saasweb.pos.model.*;
import com.saasweb.pos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/** Gastos, proveedores y el reporte de resultados del POS. */
@Service
@Transactional
public class GastoService {

    private final GastoRepository gastos;
    private final ProveedorRepository proveedores;
    private final MovimientoCajaRepository movimientosCaja;
    private final CajaRepository cajas;
    private final CajaService cajaService;
    private final VentaRepository ventas;
    private final Cajeros cajeros;
    private final ZoneId zona = ZoneId.of("America/Argentina/Buenos_Aires");

    public GastoService(GastoRepository gastos, ProveedorRepository proveedores, MovimientoCajaRepository movimientosCaja,
                        CajaRepository cajas, CajaService cajaService, VentaRepository ventas, Cajeros cajeros) {
        this.gastos = gastos;
        this.proveedores = proveedores;
        this.movimientosCaja = movimientosCaja;
        this.cajas = cajas;
        this.cajaService = cajaService;
        this.ventas = ventas;
        this.cajeros = cajeros;
    }

    // --- Proveedores ---

    @Transactional(readOnly = true)
    public List<Proveedor> proveedores() {
        return proveedores.findByTenantIdOrderByNombreAsc(TenantContext.getTenantId());
    }

    @Transactional(readOnly = true)
    public Proveedor proveedor(String id) {
        return proveedores.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> ResourceNotFoundException.of("Proveedor", id));
    }

    public Proveedor guardarProveedor(String id, ProveedorRequest req) {
        Proveedor p = id == null ? new Proveedor() : proveedor(id);
        if (id == null) {
            p.setId(UUID.randomUUID().toString());
            p.setTenantId(TenantContext.getTenantId());
        }
        p.setNombre(req.nombre().trim());
        p.setTelefono(blankToNull(req.telefono()));
        p.setNota(blankToNull(req.nota()));
        return proveedores.save(p);
    }

    public void borrarProveedor(String id) {
        Proveedor p = proveedor(id);
        if (gastos.countByTenantIdAndProveedorId(p.getTenantId(), id) > 0) {
            throw new BadRequestException("El proveedor tiene gastos cargados: no se puede borrar.");
        }
        proveedores.delete(p);
    }

    // --- Gastos ---

    @Transactional(readOnly = true)
    public List<Gasto> gastos(LocalDate desde, LocalDate hasta) {
        return gastos.findByTenantIdAndFechaBetweenOrderByFechaDescCreadoEnDesc(TenantContext.getTenantId(), desde, hasta);
    }

    public Gasto crearGasto(GastoRequest req, String dni) {
        if (req.proveedorId() != null && !req.proveedorId().isBlank()) proveedor(req.proveedorId());
        Gasto g = new Gasto();
        g.setId(UUID.randomUUID().toString());
        g.setTenantId(TenantContext.getTenantId());
        g.setFecha(req.fecha() != null ? req.fecha() : LocalDate.now(zona));
        g.setConcepto(req.concepto().trim());
        g.setCategoria(blankToNull(req.categoria()));
        g.setMonto(req.monto());
        g.setProveedorId(blankToNull(req.proveedorId()));
        g.setAutorNombre(cajeros.nombreDe(dni));
        if (req.pagadoDeCaja()) {
            MovimientoCaja m = cajaService.movimiento(MovimientoCaja.Tipo.EGRESO, req.monto(),
                    "Gasto: " + g.getConcepto(), g.getId(), dni);
            g.setMovimientoCajaId(m.getId());
        }
        return gastos.save(g);
    }

    /** Si el gasto salió de una caja ya cerrada no se borra: cambiaría un arqueo hecho. */
    public void borrarGasto(String id) {
        Gasto g = gastos.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> ResourceNotFoundException.of("Gasto", id));
        if (g.getMovimientoCajaId() != null) {
            movimientosCaja.findById(g.getMovimientoCajaId()).ifPresent(m -> {
                boolean abierta = cajas.findById(m.getCajaId()).map(Caja::abierta).orElse(false);
                if (!abierta) throw new BadRequestException("Se pagó de una caja ya cerrada: no se puede borrar.");
                movimientosCaja.delete(m);
            });
        }
        gastos.delete(g);
    }

    // --- Reporte ---

    @Transactional(readOnly = true)
    public ReporteResponse reporte(LocalDate desde, LocalDate hasta) {
        String tenantId = TenantContext.getTenantId();
        Map<Venta.MedioPago, BigDecimal> porMedio = new EnumMap<>(Venta.MedioPago.class);
        for (Venta.MedioPago m : Venta.MedioPago.values()) porMedio.put(m, BigDecimal.ZERO);
        Map<String, ArticuloVendido> porArticulo = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO, costo = BigDecimal.ZERO, sinCosto = BigDecimal.ZERO;
        int cantidad = 0;
        for (Venta v : ventas.findByTenantIdAndFechaBetween(tenantId, desde.atStartOfDay(zona).toInstant(),
                hasta.plusDays(1).atStartOfDay(zona).toInstant())) {
            if (v.getEstado() == Venta.Estado.ANULADA) continue;
            cantidad++;
            total = total.add(v.getTotal());
            porMedio.merge(v.getMedioPago(), v.getTotal(), BigDecimal::add);
            for (VentaLinea l : v.getLineas()) {
                if (l.getCostoUnitario() != null) {
                    costo = costo.add(l.getCostoUnitario().multiply(BigDecimal.valueOf(l.getCantidad())));
                } else {
                    sinCosto = sinCosto.add(l.subtotal());
                }
                String clave = l.getArticuloId() != null ? l.getArticuloId() : "varios:" + l.getNombre();
                porArticulo.merge(clave, new ArticuloVendido(l.getArticuloId(), l.getNombre(), l.getCantidad(), l.subtotal()),
                        (a, b) -> new ArticuloVendido(a.articuloId(), a.nombre(), a.unidades() + b.unidades(),
                                a.total().add(b.total())));
            }
        }
        BigDecimal totalGastos = BigDecimal.ZERO;
        Map<String, BigDecimal> porCategoria = new TreeMap<>();
        for (Gasto g : gastos.findByTenantIdAndFechaBetweenOrderByFechaDescCreadoEnDesc(tenantId, desde, hasta)) {
            totalGastos = totalGastos.add(g.getMonto());
            porCategoria.merge(g.getCategoria() == null ? "Sin categoría" : g.getCategoria(), g.getMonto(), BigDecimal::add);
        }
        BigDecimal bruta = total.subtract(costo);
        List<ArticuloVendido> top = porArticulo.values().stream()
                .sorted(Comparator.comparingInt(ArticuloVendido::unidades).reversed()).limit(10).toList();
        return new ReporteResponse(desde, hasta, cantidad, total, costo, sinCosto, bruta, totalGastos,
                bruta.subtract(totalGastos), porMedio, porCategoria, top);
    }

    private static String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}
