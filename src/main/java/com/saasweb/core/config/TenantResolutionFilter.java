package com.saasweb.core.config;

import com.saasweb.core.common.TenantContext;
import com.saasweb.core.model.Modules;
import com.saasweb.core.model.Plan;
import com.saasweb.core.service.PlanService;
import com.saasweb.core.service.TenantResolver;
import com.saasweb.core.service.TenantService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Resuelve el tenant de la request (por `Host`, ver {@link TenantResolver})
 * y lo deja en {@link TenantContext} para el resto del pipeline. Un host que
 * no es de ninguna tienda recibe 404, salvo las rutas que no dependen de una
 * tienda ({@link #TENANTLESS_PATHS}: la consulta de Caddy, swagger).
 */
@Component
public class TenantResolutionFilter extends OncePerRequestFilter {

    /**
     * Selector de tienda modo demo (ver PLAN_SAAS.md): si
     * {@code app.tenant.demo-switch-enabled} está en true y el request trae
     * este header con un slug válido, se usa ESE tenant en vez del único
     * activo. No es resolución real por dominio — es un atajo para mostrar
     * varias tiendas locales sin subdominios/DNS.
     */
    public static final String DEMO_TENANT_HEADER = "X-Demo-Tenant";

    /** Rutas que funcionan aunque el host no sea de ninguna tienda. */
    private static final String[] TENANTLESS_PATHS = {
            "/api/public/core/dominios/permitido", "/swagger-ui", "/v3/api-docs", "/error"};

    private final TenantService tenantService;
    private final TenantResolver tenantResolver;
    private final PlanService planService;

    public TenantResolutionFilter(TenantService tenantService, TenantResolver tenantResolver,
                                  PlanService planService) {
        this.tenantService = tenantService;
        this.tenantResolver = tenantResolver;
        this.planService = planService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        try {
            String tenantId = tenantResolver
                    .resolve(request.getServerName(), request.getHeader(DEMO_TENANT_HEADER))
                    .orElse(null);
            if (tenantId == null && !isTenantless(request)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"tienda_no_encontrada\",\"message\":\"No hay ninguna tienda en esta dirección.\"}");
                return;
            }
            TenantContext.set(tenantId);
            if (isPausedForVisitor(tenantId, request)) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"tienda_pausada\",\"message\":\"Esta tienda está pausada.\"}");
                return;
            }
            if (isMissingEcommerceSiteForVisitor(tenantId, request)) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\":\"sin_sitio_online\",\"message\":\"Esta tienda no tiene sitio online — es sólo punto de venta.\"}");
                return;
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Una tienda pausada (`Tenant.active = false`) deja de poder verse desde
     * afuera, pero el panel de administración sigue accesible siempre — así
     * el superadmin (o el admin de esa tienda) puede entrar a reanudarla.
     */
    private boolean isPausedForVisitor(String tenantId, HttpServletRequest request) {
        if (tenantId == null || tenantService.isActive(tenantId)) return false;
        return isPublicVisitorRequest(request);
    }

    /**
     * Tenant sólo-POS (Fase 14, ver PLAN_SAAS.md) — sin el módulo
     * `ECOMMERCE_SITE`, no tiene vidriera pública. Mismo criterio que una
     * tienda pausada: el panel de administración sigue libre siempre.
     */
    private boolean isMissingEcommerceSiteForVisitor(String tenantId, HttpServletRequest request) {
        if (tenantId == null) return false;
        Plan plan = planService.getForTenant(tenantId);
        if (plan == null || plan.hasModule(Modules.ECOMMERCE_SITE)) return false;
        return isPublicVisitorRequest(request);
    }

    /**
     * true si esta request es de un visitante anónimo del sitio público —
     * false si es del panel de administración, aunque le esté pegando a un
     * endpoint técnicamente "público" (`/api/products`, `/api/settings`,
     * `/api/param-groups`...: varias pantallas del panel reusan esos mismos
     * endpoints de sólo lectura en vez de duplicarlos bajo `/api/admin/`).
     * Este filtro corre ANTES que {@link JwtAuthFilter} (tiene que resolver
     * el tenant primero, ver el comentario en {@link JwtAuthFilter}), así
     * que todavía no hay una `Authentication` validada acá — mirar si la
     * request trae un `Authorization: Bearer` es la señal más barata
     * disponible en esta instancia del pipeline. No hace falta validarlo:
     * si es un token trucho, {@code JwtAuthFilter} lo va a ignorar más
     * adelante y el endpoint de todos modos va a exigir lo que ya exigía
     * antes (nada distinto a hoy) — esto sólo decide si el freno de "sin
     * sitio online"/"pausada" se aplica o no, no reemplaza ningún chequeo
     * de autorización real.
     */
    private boolean isPublicVisitorRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/") || path.startsWith("/api/auth/")) return false;
        String auth = request.getHeader("Authorization");
        return auth == null || !auth.startsWith("Bearer ");
    }

    private boolean isTenantless(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String p : TENANTLESS_PATHS) {
            if (path.startsWith(p)) return true;
        }
        return false;
    }
}
