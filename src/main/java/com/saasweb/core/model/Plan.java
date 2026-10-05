package com.saasweb.core.model;

import com.saasweb.core.service.PlanService;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Plan comercial: define límites y qué módulos están habilitados para los
 * tenants que lo tienen asignado (ver {@code Tenant.planId}). Es un catálogo
 * compartido — varios tenants pueden apuntar al mismo plan (ej: todos los
 * que están en "Básico"), no una fila por tenant.
 *
 * <p>Hoy (ver PLAN_SAAS.md Fase 5) sólo existe un plan, sembrado por
 * {@code PlanService.ensureDefault()} con límites deliberadamente
 * permisivos (null = sin límite) — no hay todavía precios/nombres/límites
 * de negocio definitivos. La infraestructura (el mecanismo de límites y
 * módulos) está lista; cargar los planes reales el día que se decidan es
 * sólo un INSERT/UPDATE, no requiere tocar código.</p>
 */
@Entity
@Table(name = "core_plan")
public class Plan {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    @Column(nullable = false)
    private String name;

    /**
     * Sistema al que sirve el plan (ver {@link Sistema}): una tienda sólo puede
     * tener un plan de su mismo sistema, y el plan sólo ofrece los módulos
     * que tienen sentido para ese sistema (ver {@link Modules#compatibleWith}).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Sistema sistema = Sistema.ECOMMERCE;

    /** null = sin límite. */
    private Integer maxProducts;

    /** null = sin límite. Cuenta AdminUser del tenant (no incluye al superadmin). */
    private Integer maxAdminUsers;

    /**
     * Claves de módulo habilitadas para este plan (ej: "ropa"). Hoy sólo
     * existe el módulo "ropa" y no está gateado en ningún lado todavía —
     * este campo es el mecanismo para cuando haya un segundo módulo real
     * que activar/desactivar por plan.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "core_plan_modulo", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "module_key", length = 40)
    private Set<String> enabledModules = new LinkedHashSet<>();

    /** "Powered by <plataforma>" u otro branding discreto de la plataforma en el sitio del tenant. */
    @Column(nullable = false)
    private boolean showPlatformBranding = false;

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

    public Sistema getSistema() {
        return sistema;
    }

    public void setSistema(Sistema sistema) {
        this.sistema = sistema;
    }

    public Integer getMaxProducts() {
        return maxProducts;
    }

    public void setMaxProducts(Integer maxProducts) {
        this.maxProducts = maxProducts;
    }

    public Integer getMaxAdminUsers() {
        return maxAdminUsers;
    }

    public void setMaxAdminUsers(Integer maxAdminUsers) {
        this.maxAdminUsers = maxAdminUsers;
    }

    public Set<String> getEnabledModules() {
        return enabledModules;
    }

    public void setEnabledModules(Set<String> enabledModules) {
        this.enabledModules = enabledModules;
    }

    /** Ver {@link Modules} para las claves válidas. */
    public boolean hasModule(String moduleKey) {
        return enabledModules.contains(moduleKey);
    }

    public boolean isShowPlatformBranding() {
        return showPlatformBranding;
    }

    public void setShowPlatformBranding(boolean showPlatformBranding) {
        this.showPlatformBranding = showPlatformBranding;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
