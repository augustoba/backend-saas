package com.saasweb.core.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Un cobro online con Mercado Pago, de cualquier sistema. Como
 * {@link Comprobante}, guarda su {@code origen} (sistema + id del documento:
 * un pedido, una mesa…) sin clave foránea. La referencia externa que viaja a
 * Mercado Pago es el id de este cobro.
 */
@Entity
@Table(name = "core_cobro_mp", indexes = @Index(name = "ix_core_cobro_mp_origen",
        columnList = "tenant_id, origen_sistema, origen_id"))
public class CobroMp {

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_sistema", nullable = false, length = 20)
    private Sistema origenSistema;

    @Column(name = "origen_id", nullable = false)
    private String origenId;

    @Column(length = 100)
    private String preferenceId;

    @Column(length = 500)
    private String checkoutUrl;

    @Column(length = 100)
    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCobro estado = EstadoCobro.PENDING;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Sistema getOrigenSistema() { return origenSistema; }
    public void setOrigenSistema(Sistema origenSistema) { this.origenSistema = origenSistema; }
    public String getOrigenId() { return origenId; }
    public void setOrigenId(String origenId) { this.origenId = origenId; }
    public String getPreferenceId() { return preferenceId; }
    public void setPreferenceId(String preferenceId) { this.preferenceId = preferenceId; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public EstadoCobro getEstado() { return estado; }
    public void setEstado(EstadoCobro estado) { this.estado = estado; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
