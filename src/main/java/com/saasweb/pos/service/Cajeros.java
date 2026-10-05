package com.saasweb.pos.service;

import com.saasweb.core.common.TenantContext;
import com.saasweb.core.repository.AdminUserRepository;
import org.springframework.stereotype.Component;

/** Nombre a mostrar de quien opera (cajero, quien ajustó stock) a partir de su DNI. */
@Component
public class Cajeros {

    private final AdminUserRepository usuarios;

    public Cajeros(AdminUserRepository usuarios) {
        this.usuarios = usuarios;
    }

    public String nombreDe(String dni) {
        if (dni == null || dni.isBlank()) return null;
        return usuarios.findByDniForTenant(dni, TenantContext.getTenantId())
                .map(u -> u.getNombre() + " " + u.getApellido()).orElse(dni);
    }
}
