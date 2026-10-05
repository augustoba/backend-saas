package com.saasweb.core.service;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.model.PermisoDef;
import com.saasweb.core.model.Sistema;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Junta los {@link CatalogoPermisos} del núcleo y de cada sistema: qué
 * permisos existen para una tienda según su sistema y qué roles se le crean
 * por defecto.
 */
@Service
public class PermisoService {

    public static final String ADMINISTRADOR = "Administrador";

    private final List<CatalogoPermisos> catalogos;

    public PermisoService(List<CatalogoPermisos> catalogos) {
        this.catalogos = catalogos;
    }

    /** Permisos que se pueden asignar en una tienda de ese sistema (núcleo + los del sistema). */
    public List<PermisoDef> paraSistema(Sistema sistema) {
        Map<String, PermisoDef> out = new LinkedHashMap<>();
        for (CatalogoPermisos c : catalogos) {
            if (c.sistema() == null) c.permisos().forEach(p -> out.putIfAbsent(p.clave(), p));
        }
        for (CatalogoPermisos c : catalogos) {
            if (c.sistema() == sistema) c.permisos().forEach(p -> out.putIfAbsent(p.clave(), p));
        }
        return List.copyOf(out.values());
    }

    /**
     * Lo que el admin de una tienda puede tildar en un rol: lo del sistema sin
     * lo reservado al superadmin (que tiene todo por su rol de sistema).
     */
    public List<PermisoDef> asignables(Sistema sistema) {
        return paraSistema(sistema).stream().filter(p -> !p.soloSuperadmin()).toList();
    }

    /** Todas las claves de todos los catálogos: lo que tiene el rol de sistema (superadmin). */
    public Set<String> todas() {
        Set<String> out = new LinkedHashSet<>();
        catalogos.forEach(c -> c.permisos().forEach(p -> out.add(p.clave())));
        return out;
    }

    /**
     * Roles que nacen con una tienda de ese sistema: "Administrador" con todo
     * menos lo reservado al superadmin, más los que declare el sistema.
     */
    public Map<String, Set<String>> rolesPorDefecto(Sistema sistema) {
        Map<String, Set<String>> roles = new LinkedHashMap<>();
        Set<String> admin = new LinkedHashSet<>();
        asignables(sistema).forEach(p -> admin.add(p.clave()));
        roles.put(ADMINISTRADOR, admin);
        for (CatalogoPermisos c : catalogos) {
            if (c.sistema() == sistema) roles.putAll(c.rolesPorDefecto());
        }
        return roles;
    }

    /** Falla si alguna clave no existe para ese sistema (ej: un permiso del restaurante en un ecommerce). */
    public Set<String> validar(Sistema sistema, Collection<String> claves) {
        if (claves == null || claves.isEmpty()) return new LinkedHashSet<>();
        Set<String> validas = new HashSet<>();
        asignables(sistema).forEach(p -> validas.add(p.clave()));
        Set<String> out = new LinkedHashSet<>();
        for (String c : claves) {
            if (!validas.contains(c)) throw new BadRequestException("Permiso desconocido para esta tienda: " + c);
            out.add(c);
        }
        return out;
    }
}
