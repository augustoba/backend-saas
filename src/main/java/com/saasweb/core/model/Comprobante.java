package com.saasweb.core.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Comprobante fiscal emitido con ARCA (factura o nota de crédito), aprobado o
 * no: siempre queda el registro del intento. Es del núcleo: no sabe qué es un
 * pedido, una venta de kiosco o una mesa; sólo guarda su {@code origen}
 * (sistema + id del documento que lo originó), sin clave foránea.
 */
@Entity
@Table(name = "core_comprobante", indexes = @Index(name = "ix_core_comprobante_origen",
        columnList = "tenant_id, origen_sistema, origen_id"))
public class Comprobante {

    public enum Clase { FACTURA, NOTA_CREDITO }

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_sistema", nullable = false, length = 20)
    private Sistema origenSistema;

    @Column(name = "origen_id", nullable = false)
    private String origenId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Clase clase;

    /** Tipo ARCA tal como lo devuelve la emisión (ej: "FACTURA_B", "NC_B"). */
    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(length = 500)
    private String motivo;

    @Column(length = 20)
    private String cuitComprador;

    private String cae;

    @Column(length = 10)
    private String caeVencimiento;

    private Long numero;

    private Integer puntoVenta;

    @Column(length = 500)
    private String qrUrl;

    /** Motivo del rechazo de ARCA; null si se aprobó. */
    @Column(length = 500)
    private String error;

    private String createdByDni;

    private String createdByName;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public boolean aprobado() {
        return cae != null && error == null;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Sistema getOrigenSistema() { return origenSistema; }
    public void setOrigenSistema(Sistema origenSistema) { this.origenSistema = origenSistema; }
    public String getOrigenId() { return origenId; }
    public void setOrigenId(String origenId) { this.origenId = origenId; }
    public Clase getClase() { return clase; }
    public void setClase(Clase clase) { this.clase = clase; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getCuitComprador() { return cuitComprador; }
    public void setCuitComprador(String cuitComprador) { this.cuitComprador = cuitComprador; }
    public String getCae() { return cae; }
    public void setCae(String cae) { this.cae = cae; }
    public String getCaeVencimiento() { return caeVencimiento; }
    public void setCaeVencimiento(String caeVencimiento) { this.caeVencimiento = caeVencimiento; }
    public Long getNumero() { return numero; }
    public void setNumero(Long numero) { this.numero = numero; }
    public Integer getPuntoVenta() { return puntoVenta; }
    public void setPuntoVenta(Integer puntoVenta) { this.puntoVenta = puntoVenta; }
    public String getQrUrl() { return qrUrl; }
    public void setQrUrl(String qrUrl) { this.qrUrl = qrUrl; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getCreatedByDni() { return createdByDni; }
    public void setCreatedByDni(String createdByDni) { this.createdByDni = createdByDni; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
