package com.saasweb.core.model;

import java.util.Set;

/**
 * Claves de módulo de {@link Plan#getEnabledModules()}: funciones extra que un
 * plan puede prender o apagar. Qué sitio público tiene una tienda (o si no
 * tiene) ya no es un módulo: lo decide su {@link Sistema}.
 */
public final class Modules {

    private Modules() {
    }

    /**
     * Botón "Publicar en redes" del formulario/listado de productos
     * (comparte foto + texto vía la Web Share API del celular del admin).
     */
    public static final String SOCIAL_SHARE = "SOCIAL_SHARE";

    /**
     * Cobro online con Mercado Pago — cada tenant carga sus propias
     * credenciales; este flag sólo habilita que la opción exista para el plan.
     */
    public static final String MERCADOPAGO = "MERCADOPAGO";

    /**
     * Factura electrónica real vía ARCA/AFIP — cada tenant carga su CUIT y
     * certificado. Sin este módulo sólo se emite ticket interno, no fiscal.
     */
    public static final String ARCA_INVOICING = "ARCA_INVOICING";

    /** Plantilla de datos de ejemplo de indumentaria (talles, parametrías). */
    public static final String ROPA = "ropa";

    /** Qué módulos tiene sentido ofrecerle a un plan de ese sistema. */
    public static Set<String> compatibleWith(Sistema sistema) {
        return switch (sistema) {
            case ECOMMERCE -> Set.of(ROPA, SOCIAL_SHARE, MERCADOPAGO, ARCA_INVOICING);
            case POS -> Set.of(ARCA_INVOICING);
            case RESTAURANTE -> Set.of(MERCADOPAGO, ARCA_INVOICING);
        };
    }
}
