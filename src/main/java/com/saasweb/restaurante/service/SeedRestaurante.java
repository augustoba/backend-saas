package com.saasweb.restaurante.service;

import com.saasweb.core.model.Sistema;
import com.saasweb.core.model.Tenant;
import com.saasweb.core.repository.TenantRepository;
import com.saasweb.restaurante.repository.SucursalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Al arrancar, siembra los restaurantes creados antes de que el sistema existiera (sin sucursal). */
@Component
@org.springframework.core.annotation.Order(4)
public class SeedRestaurante implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedRestaurante.class);

    private final TenantRepository tenants;
    private final SucursalRepository sucursales;
    private final InicializadorRestaurante inicializador;

    public SeedRestaurante(TenantRepository tenants, SucursalRepository sucursales, InicializadorRestaurante inicializador) {
        this.tenants = tenants;
        this.sucursales = sucursales;
        this.inicializador = inicializador;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (Tenant t : tenants.findAllByOrderByCreatedAtAsc()) {
            if (t.getSistema() != Sistema.RESTAURANTE) continue;
            if (!sucursales.findByTenantIdAndActivoTrue(t.getId()).isEmpty()) continue;
            inicializador.sembrar(t.getId());
            log.info("Restaurante '{}': datos iniciales sembrados.", t.getSlug());
        }
    }
}
