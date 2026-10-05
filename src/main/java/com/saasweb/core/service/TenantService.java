package com.saasweb.core.service;

import com.saasweb.core.model.Plan;
import com.saasweb.core.model.Rubro;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.model.Tenant;
import com.saasweb.core.repository.TenantRepository;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * CRUD mínimo de tenants. Hasta la Fase 8 (dominios reales) no hay
 * resolución por `Host` — {@code resolveCurrentTenantId()} sigue
 * resolviendo siempre "el único tenant activo", y
 * {@code TenantResolutionFilter} sólo se aparta de eso con el selector de
 * tienda modo demo (`X-Demo-Tenant`, ver PLAN_SAAS.md) para poder mostrar
 * varios tenants locales sin subdominios reales.
 */
@Service
@Transactional
public class TenantService {

    private final TenantRepository repo;
    private final AppProperties props;
    private final PlanService planService;

    public TenantService(TenantRepository repo, AppProperties props, PlanService planService) {
        this.repo = repo;
        this.props = props;
        this.planService = planService;
    }

    /**
     * Crea el tenant único de este deploy si todavía no existe, con el plan
     * por defecto ya asignado (ver PlanService.ensureDefault) — Tenant.planId
     * es NOT NULL, así que el plan se resuelve/crea en la misma operación,
     * nunca queda una fila de tenant sin plan. Idempotente.
     */
    public Tenant ensureDefault() {
        String slug = props.getTenant().getSlug();
        return repo.findBySlug(slug).orElseGet(() -> create(props.getTenant().getName(), slug, Rubro.ROPA, Sistema.ECOMMERCE, null));
    }

    /**
     * Crea un tenant nuevo. No siembra parametrías/productos — eso lo hace
     * TenantProvisioningService. {@code planId} en blanco/null = el plan por
     * defecto (ver PlanService.ensureDefault) — compat con altas que no
     * pasan por el paso del asistente que elige plan (Fase 14).
     */
    public Tenant create(String name, String slug, Rubro rubro, Sistema sistema, String planId) {
        if (repo.findBySlug(slug).isPresent()) {
            throw new BadRequestException("Ya existe una tienda con ese identificador.");
        }
        // Sin sistema explícito, manda el del plan elegido (o ecommerce si tampoco hay plan).
        Plan plan = planId != null && !planId.isBlank() ? planService.get(planId) : null;
        Sistema resolvedSistema = sistema != null ? sistema : plan != null ? plan.getSistema() : Sistema.ECOMMERCE;
        if (plan == null) plan = planService.defaultFor(resolvedSistema);
        if (plan.getSistema() != resolvedSistema) {
            throw new BadRequestException("El plan elegido es para otro sistema.");
        }
        String resolvedPlanId = plan.getId();
        Tenant t = new Tenant();
        t.setId(UUID.randomUUID().toString());
        t.setSlug(slug);
        t.setName(name);
        t.setRubro(rubro);
        t.setPlanId(resolvedPlanId);
        t.setSistema(resolvedSistema);
        return repo.save(t);
    }

    @Transactional(readOnly = true)
    public List<Tenant> findAll() {
        return repo.findAllByOrderByCreatedAtAsc();
    }

    /** Id del tenant que debe usar la request actual. Hoy: siempre el único activo (sin el selector de demo). */
    @Transactional(readOnly = true)
    public String resolveCurrentTenantId() {
        return repo.findFirstByActiveTrueOrderByCreatedAtAsc().map(Tenant::getId).orElse(null);
    }

    /** Sistema de una tienda (ver {@link Sistema}); ecommerce si no existe. */
    @Transactional(readOnly = true)
    public Sistema sistemaDe(String tenantId) {
        return repo.findById(tenantId).map(Tenant::getSistema).orElse(Sistema.ECOMMERCE);
    }

    /** Para el selector de tienda modo demo (ver TenantResolutionFilter). null si el slug no existe. */
    @Transactional(readOnly = true)
    public String resolveIdBySlug(String slug) {
        return repo.findBySlug(slug).map(Tenant::getId).orElse(null);
    }

    /** true si el tenant existe y no está pausado (ver TenantResolutionFilter). */
    @Transactional(readOnly = true)
    public boolean isActive(String tenantId) {
        return tenantId != null && repo.existsByIdAndActiveTrue(tenantId);
    }

    /** Pausar/reanudar una tienda (ver TenantResolutionFilter): pausada, su storefront deja de poder verse. */
    public Tenant setActive(String tenantId, boolean active) {
        Tenant t = repo.findById(tenantId)
                .orElseThrow(() -> new BadRequestException("La tienda no existe."));
        t.setActive(active);
        return repo.save(t);
    }
}
