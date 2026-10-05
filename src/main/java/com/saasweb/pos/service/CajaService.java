package com.saasweb.pos.service;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;
import com.saasweb.pos.dto.VentaDtos.CajaResponse;
import com.saasweb.pos.dto.VentaDtos.MovimientoCajaResponse;
import com.saasweb.pos.dto.VentaDtos.ResumenCajaResponse;
import com.saasweb.pos.model.Caja;
import com.saasweb.pos.model.MovimientoCaja;
import com.saasweb.pos.model.Venta;
import com.saasweb.pos.repository.CajaRepository;
import com.saasweb.pos.repository.MovimientoCajaRepository;
import com.saasweb.pos.repository.VentaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** La caja es el turno: una sola abierta por tienda; se cierra con arqueo del efectivo. */
@Service
@Transactional
public class CajaService {

    private final CajaRepository cajas;
    private final MovimientoCajaRepository movimientos;
    private final VentaRepository ventas;
    private final Cajeros cajeros;

    public CajaService(CajaRepository cajas, MovimientoCajaRepository movimientos, VentaRepository ventas,
                       Cajeros cajeros) {
        this.cajas = cajas;
        this.movimientos = movimientos;
        this.ventas = ventas;
        this.cajeros = cajeros;
    }

    @Transactional(readOnly = true)
    public Optional<Caja> actual() {
        return cajas.findFirstByTenantIdAndEstado(TenantContext.getTenantId(), Caja.Estado.ABIERTA);
    }

    /** La caja abierta, o 400 con un mensaje para el cajero. */
    public Caja abiertaOFalla() {
        return actual().orElseThrow(() -> new BadRequestException("No hay una caja abierta: abrí la caja para vender."));
    }

    @Transactional(readOnly = true)
    public Caja get(String id) {
        return cajas.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> ResourceNotFoundException.of("Caja", id));
    }

    public Caja abrir(BigDecimal montoInicial, String dni) {
        String tenantId = TenantContext.getTenantId();
        if (actual().isPresent()) throw new BadRequestException("Ya hay una caja abierta.");
        Caja c = new Caja();
        c.setId(UUID.randomUUID().toString());
        c.setTenantId(tenantId);
        c.setNumero(cajas.ultimoNumero(tenantId) + 1);
        c.setMontoInicial(montoInicial);
        c.setAbiertaPorDni(dni);
        c.setAbiertaPorNombre(cajeros.nombreDe(dni));
        return cajas.save(c);
    }

    public MovimientoCaja movimiento(MovimientoCaja.Tipo tipo, BigDecimal monto, String motivo, String gastoId, String dni) {
        Caja c = abiertaOFalla();
        MovimientoCaja m = new MovimientoCaja();
        m.setId(UUID.randomUUID().toString());
        m.setTenantId(c.getTenantId());
        m.setCajaId(c.getId());
        m.setTipo(tipo);
        m.setMonto(monto);
        m.setMotivo(motivo.trim());
        m.setGastoId(gastoId);
        m.setAutorDni(dni);
        m.setAutorNombre(cajeros.nombreDe(dni));
        return movimientos.save(m);
    }

    /** Arqueo: guarda lo esperado, lo contado y la diferencia. Después ya no se puede vender ni anular en ella. */
    public ResumenCajaResponse cerrar(BigDecimal efectivoContado, String nota, String dni) {
        Caja c = abiertaOFalla();
        ResumenCajaResponse r = resumen(c);
        c.setEstado(Caja.Estado.CERRADA);
        c.setCerradaEn(Instant.now());
        c.setCerradaPorDni(dni);
        c.setCerradaPorNombre(cajeros.nombreDe(dni));
        c.setEfectivoEsperado(r.efectivoEsperado());
        c.setEfectivoContado(efectivoContado);
        c.setDiferencia(efectivoContado.subtract(r.efectivoEsperado()));
        c.setNotaCierre(nota == null || nota.isBlank() ? null : nota.trim());
        cajas.save(c);
        return resumen(c);
    }

    @Transactional(readOnly = true)
    public List<Caja> historial(int limite) {
        return cajas.findByTenantIdOrderByAbiertaEnDesc(TenantContext.getTenantId(),
                PageRequest.of(0, Math.min(Math.max(limite, 1), 200)));
    }

    @Transactional(readOnly = true)
    public ResumenCajaResponse resumen(Caja c) {
        Map<Venta.MedioPago, BigDecimal> porMedio = new EnumMap<>(Venta.MedioPago.class);
        for (Venta.MedioPago m : Venta.MedioPago.values()) porMedio.put(m, BigDecimal.ZERO);
        int cantidad = 0, anuladas = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (Venta v : ventas.findByTenantIdAndCajaIdOrderByFechaDesc(c.getTenantId(), c.getId())) {
            if (v.getEstado() == Venta.Estado.ANULADA) {
                anuladas++;
                continue;
            }
            cantidad++;
            total = total.add(v.getTotal());
            porMedio.merge(v.getMedioPago(), v.getTotal(), BigDecimal::add);
        }
        List<MovimientoCaja> movs = movimientos.findByTenantIdAndCajaIdOrderByFechaAsc(c.getTenantId(), c.getId());
        BigDecimal ingresos = suma(movs, MovimientoCaja.Tipo.INGRESO);
        BigDecimal egresos = suma(movs, MovimientoCaja.Tipo.EGRESO);
        BigDecimal esperado = c.getMontoInicial().add(porMedio.get(Venta.MedioPago.EFECTIVO)).add(ingresos).subtract(egresos);
        return new ResumenCajaResponse(CajaResponse.from(c), cantidad, anuladas, total, porMedio, ingresos, egresos,
                esperado, movs.stream().map(MovimientoCajaResponse::from).toList());
    }

    private static BigDecimal suma(List<MovimientoCaja> movs, MovimientoCaja.Tipo tipo) {
        return movs.stream().filter(m -> m.getTipo() == tipo).map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
