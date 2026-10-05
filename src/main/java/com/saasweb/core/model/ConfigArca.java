package com.saasweb.core.model;

import jakarta.persistence.*;

/**
 * Credenciales de ARCA/AFIP de la tienda (su propio CUIT y certificado) y qué
 * comprobante emite por defecto. Una fila por tienda (id = tenant).
 */
@Entity
@Table(name = "core_config_arca")
public class ConfigArca {

    @Id
    private String id;

    @Column(nullable = false)
    private boolean arcaEnabled = false;

    /** true = homologación (pruebas de ARCA), false = producción. */
    @Column(nullable = false)
    private boolean arcaModoPrueba = true;

    @Column(length = 20)
    private String arcaCuit;

    private Integer arcaPuntoVenta;

    /** "MONOTRIBUTO", "EXENTO" o "RESPONSABLE_INSCRIPTO". */
    @Column(length = 40)
    private String arcaCondicionIva;

    @Column(length = 100_000)
    private String arcaCertificadoPem;

    @Column(length = 100_000)
    private String arcaClavePrivadaPem;

    /** "TICKET_INTERNO" o "FACTURA_ARCA": qué ofrece por defecto el punto de venta. */
    @Column(length = 20, nullable = false)
    private String invoiceMode = "TICKET_INTERNO";

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public boolean isArcaEnabled() { return arcaEnabled; }
    public void setArcaEnabled(boolean arcaEnabled) { this.arcaEnabled = arcaEnabled; }
    public boolean isArcaModoPrueba() { return arcaModoPrueba; }
    public void setArcaModoPrueba(boolean arcaModoPrueba) { this.arcaModoPrueba = arcaModoPrueba; }
    public String getArcaCuit() { return arcaCuit; }
    public void setArcaCuit(String arcaCuit) { this.arcaCuit = arcaCuit; }
    public Integer getArcaPuntoVenta() { return arcaPuntoVenta; }
    public void setArcaPuntoVenta(Integer arcaPuntoVenta) { this.arcaPuntoVenta = arcaPuntoVenta; }
    public String getArcaCondicionIva() { return arcaCondicionIva; }
    public void setArcaCondicionIva(String arcaCondicionIva) { this.arcaCondicionIva = arcaCondicionIva; }
    public String getArcaCertificadoPem() { return arcaCertificadoPem; }
    public void setArcaCertificadoPem(String arcaCertificadoPem) { this.arcaCertificadoPem = arcaCertificadoPem; }
    public String getArcaClavePrivadaPem() { return arcaClavePrivadaPem; }
    public void setArcaClavePrivadaPem(String arcaClavePrivadaPem) { this.arcaClavePrivadaPem = arcaClavePrivadaPem; }
    public String getInvoiceMode() { return invoiceMode; }
    public void setInvoiceMode(String invoiceMode) { this.invoiceMode = invoiceMode; }
}
