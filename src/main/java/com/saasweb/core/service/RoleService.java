package com.saasweb.core.service;

import com.saasweb.core.dto.RoleDtos;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;
import com.saasweb.core.dto.RoleDtos.RoleRequest;
import com.saasweb.core.model.PermisoDef;
import com.saasweb.core.model.Role;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.model.Tenant;
import com.saasweb.core.repository.AdminUserRepository;
import com.saasweb.core.repository.RoleRepository;
import com.saasweb.core.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class RoleService {

    private final RoleRepository repo;
    private final AdminUserRepository users;
    private final PermisoService permisos;
    private final TenantRepository tenants;

    public RoleService(RoleRepository repo, AdminUserRepository users, PermisoService permisos,
                       TenantRepository tenants) {
        this.repo = repo;
        this.users = users;
        this.permisos = permisos;
        this.tenants = tenants;
    }

    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return repo.findAllForTenant(TenantContext.getTenantId());
    }

    @Transactional(readOnly = true)
    public Role get(String id) {
        return repo.findByIdForTenant(id, TenantContext.getTenantId())
                .orElseThrow(() -> ResourceNotFoundException.of("Rol", id));
    }

    public long userCount(String roleId) {
        return users.countByRoleId(roleId);
    }

    public Role create(RoleRequest req) {
        String tenantId = TenantContext.getTenantId();
        String name = req.name().trim();
        if (repo.existsByTenantIdAndNameIgnoreCase(tenantId, name)) {
            throw new BadRequestException("Ya existe un rol con ese nombre.");
        }
        Role r = new Role();
        r.setId(UUID.randomUUID().toString());
        r.setTenantId(tenantId);
        r.setName(name);
        r.setSystem(false);
        r.setPermissions(cleanPermissions(req));
        return repo.save(r);
    }

    public Role update(String id, RoleRequest req) {
        Role r = get(id);
        if (r.isSystem()) {
            throw new BadRequestException("El rol Superadmin no se puede editar (tiene todos los permisos).");
        }
        String name = req.name().trim();
        if (!name.equalsIgnoreCase(r.getName()) && repo.existsByTenantIdAndNameIgnoreCase(r.getTenantId(), name)) {
            throw new BadRequestException("Ya existe un rol con ese nombre.");
        }
        r.setName(name);
        r.setPermissions(cleanPermissions(req));
        return repo.save(r);
    }

    public void delete(String id) {
        Role r = get(id);
        if (r.isSystem()) {
            throw new BadRequestException("El rol Superadmin no se puede borrar.");
        }
        if (users.countByRoleId(id) > 0) {
            throw new BadRequestException("Hay usuarios con este rol. Cambiales el rol antes de borrarlo.");
        }
        repo.delete(r);
    }

    /**
     * Rol del sistema: siempre TODOS los permisos (incluidos los que se agreguen a
     * futuro), no se puede editar ni borrar. Pensado para el desarrollador/dueño de
     * la plataforma, no para el admin de cada tienda (ver {@link #ensureRole}).
     * Se llama desde el DataSeeder.
     */
    public Role ensureSystemRole() {
        return repo.findFirstBySystemTrue().orElseGet(() -> {
            Role superadmin = new Role();
            superadmin.setId(UUID.randomUUID().toString());
            superadmin.setName("Superadmin");
            superadmin.setSystem(true);
            return repo.save(superadmin);
        });
    }

    /**
     * Crea los roles por defecto del sistema de la tienda ("Administrador" +
     * los que declare el sistema, ver {@link PermisoService#rolesPorDefecto}).
     * No retoca roles que ya existen.
     */
    public void ensureRolesPorDefecto(String tenantId, Sistema sistema) {
        permisos.rolesPorDefecto(sistema).forEach((name, claves) -> {
            if (repo.findByTenantIdAndNameIgnoreCase(tenantId, name).isEmpty()) {
                Role r = new Role();
                r.setId(UUID.randomUUID().toString());
                r.setTenantId(tenantId);
                r.setName(name);
                r.setSystem(false);
                r.setPermissions(new LinkedHashSet<>(claves));
                repo.save(r);
            }
        });
    }

    /**
     * Backfill: {@link #ensureRolesPorDefecto} no retoca un rol que ya existe,
     * así que un permiso nuevo nunca le llega solo a un rol viejo. Suma los que
     * falten (sólo los que existen para el sistema de la tienda), sin tocar nada
     * si ya los tiene o si el rol no existe.
     */
    public void grantPermissionsIfMissing(String tenantId, Sistema sistema, String roleName, Set<String> claves) {
        Set<String> validas = new LinkedHashSet<>();
        permisos.paraSistema(sistema).forEach(p -> validas.add(p.clave()));
        repo.findByTenantIdAndNameIgnoreCase(tenantId, roleName).ifPresent(r -> {
            boolean changed = false;
            for (String c : claves) {
                if (validas.contains(c) && r.getPermissions().add(c)) changed = true;
            }
            if (changed) repo.save(r);
        });
    }

    /** Permisos que se pueden asignar en la tienda actual (según su sistema). */
    @Transactional(readOnly = true)
    public List<PermisoDef> permisosDisponibles() {
        return permisos.asignables(sistemaActual());
    }

    /** Claves efectivas de un rol: todas si es el rol de sistema (superadmin). */
    public Set<String> efectivos(Role r) {
        return r.isSystem() ? permisos.todas() : r.getPermissions();
    }

    private Set<String> cleanPermissions(RoleRequest req) {
        return permisos.validar(sistemaActual(), req.permissions());
    }

    private Sistema sistemaActual() {
        return tenants.findById(TenantContext.requireTenantId()).map(Tenant::getSistema).orElse(Sistema.ECOMMERCE);
    }
}
