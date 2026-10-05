package com.saasweb.core.service;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;
import com.saasweb.core.config.AppProperties;
import com.saasweb.core.model.Dominio;
import com.saasweb.core.model.EstadoDominio;
import com.saasweb.core.repository.DominioRepository;
import com.saasweb.core.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Dominios propios del tenant actual: alta (PENDIENTE), verificación de DNS
 * (→ ACTIVO), principal y baja. También arma la URL pública de una tienda
 * (para links de mails y la vuelta de Mercado Pago).
 */
@Service
@Transactional
public class DominioService {

    /** Nombre de dominio razonable: etiquetas alfanuméricas con guiones, al menos un punto. */
    private static final Pattern HOST = Pattern.compile("^(?=.{4,253}$)([a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}$");

    private final DominioRepository repo;
    private final TenantRepository tenants;
    private final AppProperties props;
    private final DnsLookup dns;

    public DominioService(DominioRepository repo, TenantRepository tenants, AppProperties props, DnsLookup dns) {
        this.repo = repo;
        this.tenants = tenants;
        this.props = props;
        this.dns = dns;
    }

    @Transactional(readOnly = true)
    public List<Dominio> listar() {
        return repo.findByTenantIdOrderByCreatedAtAsc(TenantContext.requireTenantId());
    }

    public Dominio agregar(String rawHost) {
        String host = TenantResolver.normalizeHost(rawHost);
        if (host == null || !HOST.matcher(host).matches()) {
            throw new BadRequestException("Ese dominio no es válido (ej: mitienda.com.ar).");
        }
        String root = props.getPlatform().getDomain().toLowerCase();
        if (host.equals(root) || host.endsWith("." + root)) {
            throw new BadRequestException("Los subdominios de la plataforma ya vienen incluidos: no hace falta cargarlos.");
        }
        if (repo.findByHost(host).isPresent()) {
            throw new BadRequestException("Ese dominio ya está cargado en una tienda.");
        }
        String tenantId = TenantContext.requireTenantId();
        Dominio d = new Dominio();
        d.setId(UUID.randomUUID().toString());
        d.setTenantId(tenantId);
        d.setHost(host);
        // El primero que se carga queda como principal; se puede cambiar después.
        d.setPrincipal(repo.findByTenantIdOrderByCreatedAtAsc(tenantId).isEmpty());
        return repo.save(d);
    }

    /**
     * Activa el dominio si su DNS apunta a la IP del servidor
     * ({@code app.platform.server-ip}). Sin IP configurada (desarrollo) se
     * activa directo.
     */
    public Dominio verificar(String id) {
        Dominio d = propio(id);
        String serverIp = props.getPlatform().getServerIp();
        if (serverIp != null && !serverIp.isBlank()) {
            List<String> ips = dns.resolve(d.getHost());
            if (!ips.contains(serverIp.trim())) {
                throw new BadRequestException(ips.isEmpty()
                        ? "Todavía no encontramos el dominio en el DNS. Revisá el registro A (puede tardar unas horas en propagarse)."
                        : "El dominio apunta a " + String.join(", ", ips) + " y tiene que apuntar a " + serverIp.trim() + ".");
            }
        }
        d.setEstado(EstadoDominio.ACTIVO);
        return repo.save(d);
    }

    public Dominio marcarPrincipal(String id) {
        Dominio elegido = propio(id);
        for (Dominio d : repo.findByTenantIdOrderByCreatedAtAsc(elegido.getTenantId())) {
            d.setPrincipal(d.getId().equals(elegido.getId()));
        }
        return elegido;
    }

    public void eliminar(String id) {
        Dominio d = propio(id);
        repo.delete(d);
        if (d.isPrincipal()) {
            repo.findByTenantIdOrderByCreatedAtAsc(d.getTenantId()).stream().findFirst()
                    .ifPresent(otro -> otro.setPrincipal(true));
        }
    }

    /** Subdominio gratis del tenant actual, ej: "ruth.miplataforma.com". */
    @Transactional(readOnly = true)
    public String subdominio(String tenantId) {
        String slug = tenants.findById(tenantId).orElseThrow(() -> new ResourceNotFoundException("La tienda no existe.")).getSlug();
        return slug + "." + props.getPlatform().getDomain();
    }

    /**
     * URL pública de la tienda, sin barra final: el dominio propio principal
     * ACTIVO, o si no tiene, su subdominio. En desarrollo (plataforma en
     * {@code localhost}) devuelve {@code app.urls.frontend}, que es donde
     * corre el `ng serve`.
     */
    @Transactional(readOnly = true)
    public String urlPublica(String tenantId) {
        if ("localhost".equalsIgnoreCase(props.getPlatform().getDomain())) {
            return props.getUrls().getFrontend();
        }
        String host = repo.findFirstByTenantIdAndPrincipalTrueAndEstado(tenantId, EstadoDominio.ACTIVO)
                .map(Dominio::getHost)
                .orElseGet(() -> subdominio(tenantId));
        return props.getPlatform().getScheme() + "://" + host;
    }

    private Dominio propio(String id) {
        return repo.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Dominio no encontrado."));
    }

    /** Consulta de DNS aparte para poder reemplazarla en los tests. */
    @org.springframework.stereotype.Component
    public static class DnsLookup {
        public List<String> resolve(String host) {
            try {
                return Arrays.stream(InetAddress.getAllByName(host)).map(InetAddress::getHostAddress).toList();
            } catch (UnknownHostException e) {
                return List.of();
            }
        }
    }
}
