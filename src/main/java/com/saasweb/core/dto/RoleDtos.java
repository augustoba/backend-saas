package com.saasweb.core.dto;

import com.saasweb.core.model.PermisoDef;
import com.saasweb.core.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public final class RoleDtos {

    private RoleDtos() {}

    public record RoleRequest(
            @NotBlank @Size(max = 60) String name,
            Set<String> permissions
    ) {}

    public record RoleResponse(
            String id, String name, boolean system,
            List<String> permissions, long userCount
    ) {
        public static RoleResponse from(Role r, Set<String> efectivos, long userCount) {
            return new RoleResponse(
                    r.getId(), r.getName(), r.isSystem(),
                    efectivos.stream().sorted().toList(),
                    userCount);
        }
    }

    /** Catálogo de permisos disponibles (para armar los checkboxes en el panel). */
    public record PermissionInfo(String key, String label) {
        public static PermissionInfo of(PermisoDef p) {
            return new PermissionInfo(p.clave(), p.etiqueta());
        }
    }
}
