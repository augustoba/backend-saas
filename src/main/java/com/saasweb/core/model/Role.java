package com.saasweb.core.model;


import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Rol del panel: un nombre + un conjunto de claves de permiso (ver {@code PermisoService}; un rol de sistema tiene todas). El admin puede
 * crear roles y tildarles/destildarles permisos. El rol {@code system}
 * ("Superadmin") tiene todos los permisos siempre y no se puede editar ni
 * borrar. "Administrador" es un rol normal (editable) con un subconjunto fijo
 * de permisos — ver {@code RoleService.ensureRole}.
 */
@Entity
@Table(name = "core_rol", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name"}))
public class Role {

    @Id
    private String id;

    /** null = rol de plataforma (el rol de sistema "Superadmin"), compartido, no atado a un tenant. */
    @Column(name = "tenant_id")
    private String tenantId;

    @Column(nullable = false, length = 60)
    private String name;

    /** true = rol de sistema ("Superadmin"): todos los permisos, no editable. */
    @Column(nullable = false)
    private boolean system = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "core_rol_permiso", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission", length = 40)
    private Set<String> permissions = new LinkedHashSet<>();

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
