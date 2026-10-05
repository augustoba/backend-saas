package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ReporteDtos.MontoPorDia;
import com.saasweb.restaurante.dto.ReporteDtos.MontoPorEtiqueta;
import com.saasweb.restaurante.dto.ReporteDtos.ProductoVendido;
import com.saasweb.restaurante.dto.ReporteDtos.ReporteProductos;
import com.saasweb.restaurante.dto.ReporteDtos.ReportePropinas;
import com.saasweb.restaurante.dto.ReporteDtos.ReportePyL;
import com.saasweb.restaurante.dto.ReporteDtos.ReporteVentas;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Reportes de sólo lectura sobre datos que ya genera Órdenes/Gastos — no
 * agrega entidades nuevas, agrega/agrupa en memoria (mismo criterio que
 * {@code GastoService#kpis()}).
 */
@Service
public class ReporteService {

    private final OrdenRepository ordenRepository;
    private final OrdenItemRepository ordenItemRepository;
    private final PagoRepository pagoRepository;
    private final GastoRepository gastoRepository;

    public ReporteService(OrdenRepository ordenRepository, OrdenItemRepository ordenItemRepository,
                           PagoRepository pagoRepository, GastoRepository gastoRepository) {
        this.ordenRepository = ordenRepository;
        this.ordenItemRepository = ordenItemRepository;
        this.pagoRepository = pagoRepository;
        this.gastoRepository = gastoRepository;
    }

    public ReporteVentas ventas(LocalDate desde, LocalDate hasta) {
        List<Orden> pagadas = ordenesPagadasEnRango(desde, hasta);

        BigDecimal total = pagadas.stream().map(Orden::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        long cantidad = pagadas.size();
        BigDecimal ticketPromedio = cantidad == 0
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);

        Map<LocalDate, BigDecimal> porDia = new TreeMap<>();
        for (Orden o : pagadas) {
            LocalDate dia = fechaLocal(o.getFechaPago());
            porDia.merge(dia, o.getTotal(), BigDecimal::add);
        }

        List<Pago> pagos = pagoRepository.findByTenantId(tenantId()).stream()
                .filter(p -> dentroDelRango(fechaLocal(p.getFecha()), desde, hasta))
                .toList();
        Map<String, BigDecimal> porMedioPago = new LinkedHashMap<>();
        for (Pago p : pagos) {
            porMedioPago.merge(p.getMedioPago().getNombre(), p.getMonto(), BigDecimal::add);
        }

        return new ReporteVentas(total, cantidad, ticketPromedio,
                porDia.entrySet().stream().map(e -> new MontoPorDia(e.getKey(), e.getValue())).toList(),
                porMedioPago.entrySet().stream()
                        .sorted(Comparator.comparing((Map.Entry<String, BigDecimal> e) -> e.getValue()).reversed())
                        .map(e -> new MontoPorEtiqueta(e.getKey(), e.getValue())).toList());
    }

    public ReportePropinas propinas(LocalDate desde, LocalDate hasta) {
        List<Pago> pagos = pagoRepository.findByTenantId(tenantId()).stream()
                .filter(p -> dentroDelRango(fechaLocal(p.getFecha()), desde, hasta))
                .filter(p -> p.getPropina().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        BigDecimal total = pagos.stream().map(Pago::getPropina).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<LocalDate, BigDecimal> porDia = new TreeMap<>();
        for (Pago p : pagos) {
            porDia.merge(fechaLocal(p.getFecha()), p.getPropina(), BigDecimal::add);
        }
        return new ReportePropinas(total, porDia.entrySet().stream().map(e -> new MontoPorDia(e.getKey(), e.getValue())).toList());
    }

    public ReportePyL pyL(LocalDate desde, LocalDate hasta) {
        BigDecimal ingresos = ordenesPagadasEnRango(desde, hasta).stream()
                .map(Orden::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Gasto> gastosPagados = gastoRepository.findByTenantIdOrderByFechaDesc(tenantId()).stream()
                .filter(g -> g.getEstado() == Gasto.Estado.PAGADO)
                .filter(g -> g.getFechaPago() != null && dentroDelRango(g.getFechaPago(), desde, hasta))
                .toList();
        BigDecimal egresos = gastosPagados.stream().map(Gasto::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> porCategoria = gastosPagados.stream()
                .collect(Collectors.groupingBy(g -> g.getCategoriaGasto().getNombre(),
                        Collectors.reducing(BigDecimal.ZERO, Gasto::getMonto, BigDecimal::add)));

        return new ReportePyL(ingresos, egresos, ingresos.subtract(egresos),
                porCategoria.entrySet().stream()
                        .sorted(Comparator.comparing((Map.Entry<String, BigDecimal> e) -> e.getValue()).reversed())
                        .map(e -> new MontoPorEtiqueta(e.getKey(), e.getValue())).toList());
    }

    public ReporteProductos productos(LocalDate desde, LocalDate hasta) {
        List<OrdenItem> items = ordenItemRepository.findByOrdenTenantIdAndOrdenEstado(tenantId(), Orden.Estado.PAGADO)
                .stream()
                .filter(i -> i.getOrden().getFechaPago() != null
                        && dentroDelRango(fechaLocal(i.getOrden().getFechaPago()), desde, hasta))
                .toList();

        record Acumulado(long cantidad, BigDecimal total) {
        }

        Map<Long, String> nombres = new LinkedHashMap<>();
        Map<Long, Acumulado> porProducto = new LinkedHashMap<>();
        for (OrdenItem i : items) {
            Long productoId = i.getProducto().getId();
            nombres.putIfAbsent(productoId, i.getProducto().getNombre());
            porProducto.merge(productoId, new Acumulado(i.getCantidad(), i.getSubtotal()),
                    (a, b) -> new Acumulado(a.cantidad() + b.cantidad(), a.total().add(b.total())));
        }

        List<ProductoVendido> productos = porProducto.entrySet().stream()
                .map(e -> new ProductoVendido(e.getKey(), nombres.get(e.getKey()), e.getValue().cantidad(), e.getValue().total()))
                .sorted(Comparator.comparingLong(ProductoVendido::cantidad).reversed())
                .toList();

        return new ReporteProductos(productos);
    }

    private List<Orden> ordenesPagadasEnRango(LocalDate desde, LocalDate hasta) {
        return ordenRepository.findByTenantIdOrderByFechaCreacionDesc(tenantId()).stream()
                .filter(o -> o.getEstado() == Orden.Estado.PAGADO)
                .filter(o -> o.getFechaPago() != null && dentroDelRango(fechaLocal(o.getFechaPago()), desde, hasta))
                .toList();
    }

    private boolean dentroDelRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    private LocalDate fechaLocal(java.time.Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.systemDefault());
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
