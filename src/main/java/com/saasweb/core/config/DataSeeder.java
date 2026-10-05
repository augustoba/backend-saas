package com.saasweb.core.config;

import com.saasweb.core.model.Tenant;
import com.saasweb.core.service.AuthService;
import com.saasweb.core.service.ConfiguracionService;
import com.saasweb.core.service.PermisoService;
import com.saasweb.core.service.PermisosNucleo;
import com.saasweb.core.service.PlanService;
import com.saasweb.core.service.RoleService;
import com.saasweb.core.service.TenantService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Lo mínimo del núcleo en cada arranque: la tienda piloto del deploy, los
 * planes de cada sistema, los roles por defecto de cada tienda y los usuarios
 * iniciales (admin de la piloto y superadmin). Corre primero; después cada
 * sistema siembra lo suyo (ej: {@code SeedEcommerce}).
 */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final TenantService tenantService;
    private final PlanService planService;
    private final RoleService roleService;
    private final AuthService authService;
    private final ConfiguracionService configuracion;

    public DataSeeder(TenantService tenantService, PlanService planService, RoleService roleService,
                      AuthService authService, ConfiguracionService configuracion) {
        this.tenantService = tenantService;
        this.planService = planService;
        this.roleService = roleService;
        this.authService = authService;
        this.configuracion = configuracion;
    }

    @Override
    public void run(String... args) {
        // Tienda piloto de este deploy: primero, todo lo demás la asume creada.
        String tenantId = tenantService.ensureDefault().getId();
        // Plan de partida de cada sistema (el asistente "Crear tienda" elige entre ellos).
        planService.ensureDefault();
        planService.ensurePosPlan();
        planService.ensureRestaurantePlan();
        // Superadmin: rol de sistema, siempre todos los permisos, no pertenece a ninguna tienda.
        roleService.ensureSystemRole();
        // "Administrador" + los roles que declare el sistema de cada tienda (ver PermisoService).
        for (Tenant t : tenantService.findAll()) {
            roleService.ensureRolesPorDefecto(t.getId(), t.getSistema());
        }
        authService.ensureInitialAdmin(tenantId);
        authService.ensureInitialSuperadmin();
        configuracion.identidad(tenantId);
        backfillNewPermissions();
    }

    /**
     * {@code ensureRolesPorDefecto} no retoca roles ya existentes: los permisos
     * que se suman después del primer arranque de una tienda se agregan acá (sin
     * efecto si ya los tiene o si no aplican a su sistema).
     */
    private void backfillNewPermissions() {
        for (Tenant t : tenantService.findAll()) {
            roleService.grantPermissionsIfMissing(t.getId(), t.getSistema(), PermisoService.ADMINISTRADOR,
                    Set.of("EXPENSES_MANAGE", "FINANCE_VIEW", "STOCK_MOVEMENTS_VIEW", PermisosNucleo.DOMAINS_MANAGE));
        }
    }
}
