package com.saasweb.core.controller;

import com.saasweb.core.common.TenantContext;
import com.saasweb.core.config.AppProperties;
import com.saasweb.core.dto.DominioDtos.DominioRequest;
import com.saasweb.core.dto.DominioDtos.DominioResponse;
import com.saasweb.core.dto.DominioDtos.DominiosResponse;
import com.saasweb.core.service.DominioService;
import com.saasweb.core.service.TenantResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Dominios propios del tenant actual + la consulta que hace Caddy antes de
 * emitir un certificado (on-demand TLS).
 */
@RestController
public class DominioController {

    private final DominioService service;
    private final TenantResolver resolver;
    private final AppProperties props;

    public DominioController(DominioService service, TenantResolver resolver, AppProperties props) {
        this.service = service;
        this.resolver = resolver;
        this.props = props;
    }

    @GetMapping("/api/core/dominios")
    @PreAuthorize("hasAuthority('DOMAINS_MANAGE')")
    public DominiosResponse listar() {
        String tenantId = TenantContext.requireTenantId();
        return new DominiosResponse(service.subdominio(tenantId), service.urlPublica(tenantId),
                props.getPlatform().getServerIp(),
                service.listar().stream().map(DominioResponse::from).toList());
    }

    @PostMapping("/api/core/dominios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('DOMAINS_MANAGE')")
    public DominioResponse agregar(@Valid @RequestBody DominioRequest req) {
        return DominioResponse.from(service.agregar(req.host()));
    }

    @PostMapping("/api/core/dominios/{id}/verificar")
    @PreAuthorize("hasAuthority('DOMAINS_MANAGE')")
    public DominioResponse verificar(@PathVariable String id) {
        return DominioResponse.from(service.verificar(id));
    }

    @PutMapping("/api/core/dominios/{id}/principal")
    @PreAuthorize("hasAuthority('DOMAINS_MANAGE')")
    public DominioResponse principal(@PathVariable String id) {
        return DominioResponse.from(service.marcarPrincipal(id));
    }

    @DeleteMapping("/api/core/dominios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('DOMAINS_MANAGE')")
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }

    /**
     * Caddy (`on_demand_tls { ask ... }`) pregunta acá antes de emitir un
     * certificado: 200 = adelante, 404 = no es de ninguna tienda activa. Así
     * nadie puede apuntar un dominio cualquiera al servidor y hacernos pedir
     * certificados a Let's Encrypt.
     */
    @GetMapping("/api/public/core/dominios/permitido")
    public ResponseEntity<Void> permitido(@RequestParam String domain) {
        return resolver.certificadoPermitido(domain) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
