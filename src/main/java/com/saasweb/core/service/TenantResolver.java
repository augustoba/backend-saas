package com.saasweb.core.service;

import com.saasweb.core.config.AppProperties;
import com.saasweb.core.model.EstadoDominio;
import com.saasweb.core.model.Tenant;
import com.saasweb.core.repository.DominioRepository;
import com.saasweb.core.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * Decide a qué tenant pertenece una request a partir del {@code Host}:
 * <ol>
 *   <li>Dominio propio ACTIVO ({@code core_dominio}), por coincidencia exacta.</li>
 *   <li>{@code <slug>.<dominio de plataforma>} (ej: {@code ruth.miplataforma.com}).</li>
 *   <li>El dominio raíz de la plataforma (o {@code localhost}/{@code 127.0.0.1}):
 *       la tienda por defecto del deploy, o la del header {@code X-Demo-Tenant}
 *       si el selector de demo está prendido.</li>
 * </ol>
 * Si nada coincide devuelve vacío y {@code TenantResolutionFilter} contesta 404.
 */
@Service
@Transactional(readOnly = true)
public class TenantResolver {

    private final TenantRepository tenants;
    private final DominioRepository dominios;
    private final AppProperties props;

    public TenantResolver(TenantRepository tenants, DominioRepository dominios, AppProperties props) {
        this.tenants = tenants;
        this.dominios = dominios;
        this.props = props;
    }

    public Optional<String> resolve(String rawHost, String demoSlug) {
        String host = normalizeHost(rawHost);
        if (host == null) return Optional.empty();

        Optional<String> propio = dominios.findByHostAndEstado(host, EstadoDominio.ACTIVO)
                .map(d -> d.getTenantId());
        if (propio.isPresent()) return propio;

        String slug = subdomainSlug(host);
        if (slug != null) return tenants.findBySlug(slug).map(Tenant::getId);

        if (isPlatformRoot(host)) {
            if (props.getTenant().isDemoSwitchEnabled() && demoSlug != null && !demoSlug.isBlank()) {
                Optional<String> demo = tenants.findBySlug(demoSlug.trim()).map(Tenant::getId);
                if (demo.isPresent()) return demo;
            }
            return tenants.findBySlug(props.getTenant().getSlug()).map(Tenant::getId)
                    .or(() -> tenants.findFirstByActiveTrueOrderByCreatedAtAsc().map(Tenant::getId));
        }
        return Optional.empty();
    }

    /**
     * true si el host puede recibir un certificado (pregunta de Caddy, ver
     * {@code DominioController#permitido}): dominio propio ACTIVO de una tienda
     * activa, o subdominio de una tienda activa. El dominio raíz lo maneja la
     * configuración fija de Caddy, no esta consulta.
     */
    public boolean certificadoPermitido(String rawHost) {
        String host = normalizeHost(rawHost);
        if (host == null) return false;
        Optional<String> tenantId = dominios.findByHostAndEstado(host, EstadoDominio.ACTIVO)
                .map(d -> d.getTenantId());
        if (tenantId.isEmpty()) {
            String slug = subdomainSlug(host);
            if (slug != null) tenantId = tenants.findBySlug(slug).map(Tenant::getId);
        }
        return tenantId.map(tenants::existsByIdAndActiveTrue).orElse(false);
    }

    /** Minúsculas, sin puerto ni punto final. null si viene vacío. */
    public static String normalizeHost(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String h = raw.trim().toLowerCase(Locale.ROOT);
        if (h.startsWith("[")) return h; // IPv6 literal: no hay tiendas ahí
        int colon = h.indexOf(':');
        if (colon >= 0) h = h.substring(0, colon);
        if (h.endsWith(".")) h = h.substring(0, h.length() - 1);
        return h.isEmpty() ? null : h;
    }

    /** "ruth" para "ruth.miplataforma.com"; null si no es un subdominio de un solo nivel de la plataforma. */
    private String subdomainSlug(String host) {
        String suffix = "." + platformDomain();
        if (!host.endsWith(suffix)) return null;
        String slug = host.substring(0, host.length() - suffix.length());
        if (slug.isEmpty() || slug.contains(".") || slug.equals("www")) return null;
        return slug;
    }

    private boolean isPlatformRoot(String host) {
        String root = platformDomain();
        return host.equals(root) || host.equals("www." + root)
                || host.equals("localhost") || host.equals("127.0.0.1");
    }

    private String platformDomain() {
        return props.getPlatform().getDomain().trim().toLowerCase(Locale.ROOT);
    }
}
