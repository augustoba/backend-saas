package com.saasweb.pos.service;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;
import com.saasweb.core.model.Comprobante;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.service.ArcaInvoiceService;
import com.saasweb.core.service.ArcaInvoiceService.InvoiceLine;
import com.saasweb.core.service.ArcaInvoiceService.InvoiceResult;
import com.saasweb.core.service.ComprobanteService;
import com.saasweb.core.service.ConfiguracionService;
import com.saasweb.pos.dto.VentaDtos.ItemRequest;
import com.saasweb.pos.dto.VentaDtos.VentaRequest;
import com.saasweb.pos.model.Articulo;
import com.saasweb.pos.model.Caja;
import com.saasweb.pos.model.MovimientoStock;
import com.saasweb.pos.model.Venta;
import com.saasweb.pos.model.VentaLinea;
import com.saasweb.pos.repository.CajaRepository;
import com.saasweb.pos.repository.VentaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Ventas del mostrador. Sin caja abierta no se vende. El stock baja aunque no
 * alcance (queda negativo, ver spec §5.1). Si la tienda factura con ARCA, la
 * venta se factura con el núcleo; si ARCA falla, la venta igual queda (como
 * ticket interno con el error) y se puede reintentar.
 */
@Service
@Transactional
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    private final VentaRepository ventas;
    private final CajaRepository cajas;
    private final CajaService cajaService;
    private final ArticuloService articuloService;
    private final Cajeros cajeros;
    private final ConfiguracionService configuracion;
    private final ArcaInvoiceService arca;
    private final ComprobanteService comprobantes;

    public VentaService(VentaRepository ventas, CajaRepository cajas, CajaService cajaService,
                        ArticuloService articuloService, Cajeros cajeros, ConfiguracionService configuracion,
                        ArcaInvoiceService arca, ComprobanteService comprobantes) {
        this.ventas = ventas;
        this.cajas = cajas;
        this.cajaService = cajaService;
        this.articuloService = articuloService;
        this.cajeros = cajeros;
        this.configuracion = configuracion;
        this.arca = arca;
        this.comprobantes = comprobantes;
    }

    @Transactional(readOnly = true)
    public Venta get(String id) {
        return ventas.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> ResourceNotFoundException.of("Venta", id));
    }

    @Transactional(readOnly = true)
    public List<Venta> listar(String cajaId, int limite) {
        String tenantId = TenantContext.getTenantId();
        return cajaId != null
                ? ventas.findByTenantIdAndCajaIdOrderByFechaDesc(tenantId, cajaId)
                : ventas.findByTenantIdOrderByFechaDesc(tenantId, PageRequest.of(0, Math.min(Math.max(limite, 1), 500)));
    }

    public Venta vender(VentaRequest req, String dni) {
        Caja caja = cajaService.abiertaOFalla();
        String tenantId = caja.getTenantId();
        Venta v = new Venta();
        v.setId(UUID.randomUUID().toString());
        v.setTenantId(tenantId);
        v.setNumero(ventas.ultimoNumero(tenantId) + 1);
        v.setCajaId(caja.getId());
        v.setCajeroDni(dni);
        v.setCajeroNombre(cajeros.nombreDe(dni));
        v.setMedioPago(req.medioPago());
        v.setReferencia(blankToNull(req.referencia()));
        v.setCuitComprador(blankToNull(req.cuitComprador()));

        BigDecimal total = BigDecimal.ZERO;
        for (ItemRequest item : req.items()) {
            VentaLinea l = new VentaLinea();
            l.setId(UUID.randomUUID().toString());
            l.setCantidad(item.cantidad());
            if (item.articuloId() != null) {
                Articulo a = articuloService.get(item.articuloId());
                if (a.isArchivado()) throw new BadRequestException("\"" + a.getNombre() + "\" está archivado.");
                l.setArticuloId(a.getId());
                l.setNombre(a.getNombre());
                l.setPrecioUnitario(a.getPrecio());
                l.setIva(a.getIva());
                l.setCostoUnitario(a.getCosto());
            } else {
                if (item.precio() == null || item.precio().signum() <= 0) {
                    throw new BadRequestException("Un importe suelto necesita un precio mayor a cero.");
                }
                l.setNombre(item.nombre() == null || item.nombre().isBlank() ? "Varios" : item.nombre().trim());
                l.setPrecioUnitario(item.precio());
            }
            v.getLineas().add(l);
            total = total.add(l.subtotal());
        }
        v.setTotal(total);

        if (req.medioPago() == Venta.MedioPago.EFECTIVO && req.montoEntregado() != null) {
            if (req.montoEntregado().compareTo(total) < 0) {
                throw new BadRequestException("El efectivo entregado no alcanza para el total.");
            }
            v.setMontoEntregado(req.montoEntregado());
            v.setVuelto(req.montoEntregado().subtract(total));
        }
        v = ventas.save(v);

        for (VentaLinea l : v.getLineas()) {
            if (l.getArticuloId() == null) continue;
            Articulo a = articuloService.get(l.getArticuloId());
            articuloService.moverStock(a, -l.getCantidad(), MovimientoStock.Motivo.VENTA, v.getId(), v.codigo(), null, dni);
        }
        facturarSiCorresponde(v);
        return v;
    }

    /** Reintenta la factura de una venta que quedó como ticket interno porque ARCA falló. */
    public Venta reintentarFactura(String id) {
        Venta v = get(id);
        if (v.getEstado() == Venta.Estado.ANULADA) throw new BadRequestException("La venta está anulada.");
        if (v.facturada()) throw new BadRequestException("La venta ya está facturada.");
        if (!facturaConArca()) throw new BadRequestException("La tienda no está facturando con ARCA.");
        facturarSiCorresponde(v);
        return v;
    }

    /**
     * Anula una venta de la caja abierta: repone el stock y, si estaba
     * facturada, emite la nota de crédito. Si ARCA rechaza la nota de crédito
     * la venta NO se anula (quedaría la factura sin su contrapartida).
     */
    public Venta anular(String id, String motivo, String dni) {
        Venta v = get(id);
        if (v.getEstado() == Venta.Estado.ANULADA) throw new BadRequestException("La venta ya estaba anulada.");
        Caja caja = cajas.findByIdAndTenantId(v.getCajaId(), v.getTenantId()).orElseThrow();
        if (!caja.abierta()) throw new BadRequestException("Sólo se anulan ventas de la caja abierta.");

        if (v.facturada()) {
            Comprobante f = v.getFactura();
            InvoiceResult nc = arca.emitirNotaCredito(v.getTenantId(), f.getTipo(), f.getPuntoVenta(), f.getNumero(),
                    v.getCuitComprador(), v.getTotal());
            if (!nc.aprobado()) throw new BadRequestException("ARCA rechazó la nota de crédito: " + nc.error());
            comprobantes.registrar(origen(v), Comprobante.Clase.NOTA_CREDITO, nc, v.getTotal(), motivo,
                    v.getCuitComprador(), new ComprobanteService.Autor(dni, cajeros.nombreDe(dni)));
        }
        for (VentaLinea l : v.getLineas()) {
            if (l.getArticuloId() == null) continue;
            Articulo a = articuloService.get(l.getArticuloId());
            articuloService.moverStock(a, l.getCantidad(), MovimientoStock.Motivo.ANULACION, v.getId(),
                    "Anulación " + v.codigo(), null, dni);
        }
        v.setEstado(Venta.Estado.ANULADA);
        v.setAnuladaEn(Instant.now());
        v.setAnuladaPorNombre(cajeros.nombreDe(dni));
        v.setMotivoAnulacion(blankToNull(motivo));
        return ventas.save(v);
    }

    private boolean facturaConArca() {
        return "FACTURA_ARCA".equals(configuracion.arca().getInvoiceMode()) && arca.isAvailable();
    }

    private void facturarSiCorresponde(Venta v) {
        if (!facturaConArca()) return; // ticket interno
        InvoiceResult result;
        try {
            List<InvoiceLine> lineas = v.getLineas().stream().map(l -> new InvoiceLine(l.subtotal(), l.getIva())).toList();
            result = arca.emitirFactura(v.getTenantId(), lineas, null, v.getCuitComprador());
        } catch (Exception e) {
            log.warn("No se pudo facturar la venta {}: {}", v.codigo(), e.getMessage());
            result = new InvoiceResult(false, null, null, null, null, null, null, e.getMessage());
        }
        v.setFactura(comprobantes.registrar(origen(v), Comprobante.Clase.FACTURA, result, v.getTotal(), null,
                v.getCuitComprador(), new ComprobanteService.Autor(v.getCajeroDni(), v.getCajeroNombre())));
    }

    private static ComprobanteService.Origen origen(Venta v) {
        return new ComprobanteService.Origen(v.getTenantId(), Sistema.POS, v.getId());
    }

    private static String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}
