package com.saasweb.core.service;

import com.saasweb.core.model.PermisoDef;
import com.saasweb.core.model.Sistema;
import org.springframework.stereotype.Component;

import java.util.List;

/** Permisos del núcleo: valen en cualquier sistema. */
@Component
public class PermisosNucleo implements CatalogoPermisos {

    public static final String USERS_MANAGE = "USERS_MANAGE";
    public static final String PAYMENTS_MANAGE = "PAYMENTS_MANAGE";
    public static final String DOMAINS_MANAGE = "DOMAINS_MANAGE";
    public static final String PLATFORM_SETTINGS_MANAGE = "PLATFORM_SETTINGS_MANAGE";

    @Override
    public Sistema sistema() {
        return null;
    }

    @Override
    public List<PermisoDef> permisos() {
        return List.of(
                PermisoDef.de(USERS_MANAGE, "Usuarios y roles"),
                PermisoDef.de(PAYMENTS_MANAGE, "Medios de pago"),
                PermisoDef.de(DOMAINS_MANAGE, "Dominios propios"),
                new PermisoDef(PLATFORM_SETTINGS_MANAGE, "Configuración de plataforma", true));
    }
}
