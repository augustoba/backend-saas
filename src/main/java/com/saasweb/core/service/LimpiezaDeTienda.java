package com.saasweb.core.service;

/**
 * Borra los datos que un sistema guardó para una tienda (baja definitiva). El
 * núcleo llama a todas las implementaciones antes de borrar lo suyo, sin saber
 * qué tablas tiene cada sistema.
 */
public interface LimpiezaDeTienda {

    void borrarDatosDe(String tenantId);
}
