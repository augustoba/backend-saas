package com.saasweb.restaurante.service;

import com.saasweb.core.model.PermisoDef;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.service.CatalogoPermisos;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Permisos del restaurante. Vacío hasta la Etapa 4 (cuando se porte el
 * sistema): mientras tanto una tienda restaurante sólo tiene los del núcleo.
 */
@Component
public class PermisosRestaurante implements CatalogoPermisos {

    @Override
    public Sistema sistema() {
        return Sistema.RESTAURANTE;
    }

    @Override
    public List<PermisoDef> permisos() {
        return List.of();
    }
}
