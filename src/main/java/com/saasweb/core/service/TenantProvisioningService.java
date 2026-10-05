package com.saasweb.core.service;

import com.saasweb.core.dto.TenantAdminDtos.TenantCreateRequest;
import com.saasweb.core.model.Rubro;
import com.saasweb.core.model.Tenant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Alta de una tienda: el núcleo crea el tenant, sus roles por defecto, su
 * administrador (si vino en el pedido) y su identidad; después el
 * {@link InicializadorDeSistema} de su sistema siembra lo propio (el ecommerce,
 * su configuración y productos de ejemplo). Todo en una transacción: la tienda
 * nace ya configurada con lo que juntó el asistente "Crear tienda".
 */
@Service
@Transactional
public class TenantProvisioningService {

    private final TenantService tenantService;
    private final RoleService roleService;
    private final AuthService authService;
    private final ConfiguracionService configuracion;
    private final List<InicializadorDeSistema> inicializadores;

    public TenantProvisioningService(TenantService tenantService, RoleService roleService, AuthService authService,
                                     ConfiguracionService configuracion, List<InicializadorDeSistema> inicializadores) {
        this.tenantService = tenantService;
        this.roleService = roleService;
        this.authService = authService;
        this.configuracion = configuracion;
        this.inicializadores = inicializadores;
    }

    public Tenant provision(TenantCreateRequest req) {
        Rubro rubro = req.rubro();
        Tenant tenant = tenantService.create(req.name().trim(), req.slug().trim(), rubro, req.sistema(), req.planId());
        String tenantId = tenant.getId();

        roleService.ensureRolesPorDefecto(tenantId, tenant.getSistema());
        if (req.admin() != null) {
            var a = req.admin();
            authService.crearAdminInicial(tenantId, a.nombre(), a.apellido(), a.dni(), a.email(), a.password());
        }

        configuracion.crearPara(tenantId, req.name().trim(), RubroImages.logo(rubro.getLogoEmoji(), rubro.getLogoColor()));
        configuracion.aplicarIdentidadInicial(tenantId, blankToNull(req.brandColor()), blankToNull(req.whatsappNumber()),
                blankToNull(req.instagram()), blankToNull(req.facebookUrl()), blankToNull(req.logoUrl()),
                blankToNull(req.logoShape()));

        for (InicializadorDeSistema i : inicializadores) {
            if (i.aplicaA(tenant.getSistema())) i.inicializar(tenant, req);
        }
        return tenant;
    }

    private static String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}
