package com.saasweb.core.model;

/**
 * PENDIENTE = cargado pero todavía sin verificar que el DNS apunte al
 * servidor: no resuelve el tenant ni se le emite certificado. ACTIVO = listo.
 */
public enum EstadoDominio {
    PENDIENTE,
    ACTIVO
}
