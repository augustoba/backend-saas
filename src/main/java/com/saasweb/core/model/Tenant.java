package com.saasweb.core.model;


import jakarta.persistence.*;

import java.time.Instant;

/**
 * Un comercio de la plataforma. Usa un único {@link Sistema} (ecommerce, POS o
 * restaurante). Se resuelve por el `Host` de cada request: dominio propio
 * ({@link Dominio}) o subdominio `<slug>.<dominio de plataforma>` (ver
 * {@code TenantResolver}).
 */
@Entity
@Table(name = "core_tenant")
public class Tenant {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    @Column(nullable = false)
    private String name;

    /** Plan asignado (ver {@code core.plan.Plan}). Nunca null tras el seed. */
    @Column(name = "plan_id", nullable = false)
    private String planId;

    /** Rubro elegido al crear el tenant (ver {@link Rubro}). Nunca null tras el seed. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rubro rubro;

    /** Sistema que usa la tienda (ver {@link Sistema}). Uno solo por tenant. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Sistema sistema = Sistema.ECOMMERCE;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public Rubro getRubro() {
        return rubro;
    }

    public void setRubro(Rubro rubro) {
        this.rubro = rubro;
    }

    public Sistema getSistema() {
        return sistema;
    }

    public void setSistema(Sistema sistema) {
        this.sistema = sistema;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
