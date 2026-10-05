package com.saasweb.core.model;

/**
 * Qué sistema usa un tenant (ver el spec de la reestructuración). Un tenant
 * usa uno solo: no se comparten productos ni stock entre sistemas. Decide qué
 * panel ve el admin, qué sitio público tiene y qué módulos puede tener su plan.
 */
public enum Sistema {
    ECOMMERCE,
    POS,
    RESTAURANTE
}
