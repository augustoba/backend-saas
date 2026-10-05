package com.saasweb.core.service;

import com.saasweb.core.model.PermisoDef;
import com.saasweb.core.model.Sistema;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Permisos que declara cada parte de la plataforma. El núcleo declara los
 * suyos ({@link PermisosNucleo}, {@code sistema() == null}: aplican a todas
 * las tiendas) y cada sistema declara los propios desde su paquete, así el
 * núcleo nunca los conoce por nombre (ver el spec de la reestructuración).
 * {@link PermisoService} junta todos los beans que implementan esto.
 */
public interface CatalogoPermisos {

    /** Sistema al que aplican; null = del núcleo, para cualquier sistema. */
    Sistema sistema();

    List<PermisoDef> permisos();

    /**
     * Roles por defecto además de "Administrador" (que siempre tiene todos los
     * permisos de la tienda), con sus claves. Ej: "Vendedor" en el ecommerce.
     */
    default Map<String, Set<String>> rolesPorDefecto() {
        return Map.of();
    }
}
