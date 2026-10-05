package com.saasweb.core.service;

import com.saasweb.core.model.Comprobante;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.repository.ComprobanteRepository;
import com.saasweb.core.service.ArcaInvoiceService.InvoiceResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Registro de comprobantes del núcleo: cada sistema emite con
 * {@link ArcaInvoiceService} y guarda acá el resultado con su origen
 * (qué documento suyo lo originó). Así ecommerce, POS y restaurante facturan
 * con el mismo código y el historial vive en un solo lugar.
 */
@Service
@Transactional
public class ComprobanteService {

    /** Documento de un sistema que originó el comprobante (ej: ECOMMERCE + id del pedido). */
    public record Origen(String tenantId, Sistema sistema, String id) {
    }

    /** Quién lo pidió (para el historial). */
    public record Autor(String dni, String nombre) {
    }

    private final ComprobanteRepository repo;

    public ComprobanteService(ComprobanteRepository repo) {
        this.repo = repo;
    }

    /** Guarda el resultado de una emisión, aprobada o no (siempre queda el intento). */
    public Comprobante registrar(Origen origen, Comprobante.Clase clase, InvoiceResult result, BigDecimal importe,
                                 String motivo, String cuitComprador, Autor autor) {
        Comprobante c = new Comprobante();
        c.setId(UUID.randomUUID().toString());
        c.setTenantId(origen.tenantId());
        c.setOrigenSistema(origen.sistema());
        c.setOrigenId(origen.id());
        c.setClase(clase);
        c.setTipo(result.tipo());
        c.setImporte(importe);
        c.setMotivo(motivo == null || motivo.isBlank() ? null : motivo.trim());
        c.setCuitComprador(cuitComprador);
        if (autor != null) {
            c.setCreatedByDni(autor.dni());
            c.setCreatedByName(autor.nombre());
        }
        if (result.aprobado()) {
            c.setCae(result.cae());
            c.setCaeVencimiento(result.caeVencimiento());
            c.setNumero(result.numero());
            c.setPuntoVenta(result.puntoVenta());
            c.setQrUrl(result.qrUrl());
        } else {
            c.setError(result.error());
        }
        return repo.save(c);
    }

    @Transactional(readOnly = true)
    public List<Comprobante> deOrigen(Origen origen, Comprobante.Clase clase) {
        return repo.findByTenantIdAndOrigenSistemaAndOrigenIdAndClaseOrderByCreatedAtDesc(
                origen.tenantId(), origen.sistema(), origen.id(), clase);
    }
}
