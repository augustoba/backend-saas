package com.saasweb.core.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Dominio propio de un tenant (ej: {@code tiendaruth.com.ar}), además del
 * subdominio gratis {@code <slug>.<dominio de plataforma>} que tiene siempre.
 * Un tenant puede tener varios; el {@code principal} es el que se usa para
 * armar links (mails, vuelta de Mercado Pago) y al que redirigen los demás.
 */
@Entity
@Table(name = "core_dominio")
public class Dominio {

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    /** Siempre en minúsculas y sin puerto. Único en toda la plataforma. */
    @Column(nullable = false, unique = true)
    private String host;

    @Column(nullable = false)
    private boolean principal = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDominio estado = EstadoDominio.PENDIENTE;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }
    public EstadoDominio getEstado() { return estado; }
    public void setEstado(EstadoDominio estado) { this.estado = estado; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
