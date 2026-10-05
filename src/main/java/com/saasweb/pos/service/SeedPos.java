package com.saasweb.pos.service;

import com.saasweb.core.model.Sistema;
import com.saasweb.core.model.Tenant;
import com.saasweb.core.repository.TenantRepository;
import com.saasweb.pos.repository.ArticuloRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Al arrancar, siembra los artículos de ejemplo en los puntos de venta que no
 * tienen ninguno (los creados antes de que el POS tuviera tablas propias).
 * Después del seed del núcleo ({@code @Order(1)}).
 */
@Component
@org.springframework.core.annotation.Order(3)
public class SeedPos implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedPos.class);

    private final TenantRepository tenants;
    private final ArticuloRepository articulos;
    private final InicializadorPos inicializador;

    public SeedPos(TenantRepository tenants, ArticuloRepository articulos, InicializadorPos inicializador) {
        this.tenants = tenants;
        this.articulos = articulos;
        this.inicializador = inicializador;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (Tenant t : tenants.findAllByOrderByCreatedAtAsc()) {
            if (t.getSistema() != Sistema.POS) continue;
            if (!articulos.findByTenantIdAndArchivadoOrderByNombreAsc(t.getId(), false).isEmpty()
                    || !articulos.findByTenantIdAndArchivadoOrderByNombreAsc(t.getId(), true).isEmpty()) continue;
            inicializador.sembrar(t.getId());
            log.info("POS '{}': artículos de ejemplo sembrados.", t.getSlug());
        }
    }
}
