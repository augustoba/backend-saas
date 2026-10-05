package com.saasweb.core.service;

import com.saasweb.core.model.ConfigImagenes;

import java.util.Map;

/**
 * Un servicio donde se guardan las fotos de las tiendas (hoy Cloudinary; el
 * spec prevé Cloudflare R2). El navegador sube directo al proveedor —el
 * archivo no pasa por nuestro servidor— siguiendo el {@link TicketSubida} que
 * arma acá cada implementación. Sumar un proveedor = otra implementación +
 * su valor en {@code ConfigImagenes.proveedor}; los sistemas no se enteran.
 */
public interface ProveedorImagenes {

    /** Valor de {@code ConfigImagenes.proveedor} que atiende (ej: "CLOUDINARY"). */
    String clave();

    /** true si la tienda tiene cargado lo necesario para subir con este proveedor. */
    boolean configurado(ConfigImagenes cfg);

    /** Instrucciones para subir UNA imagen a {@code carpeta}/{@code nombre}. */
    TicketSubida ticket(ConfigImagenes cfg, String carpeta, String nombre);

    /**
     * Borra estas imágenes (por su URL pública). <b>Best effort</b>: nunca tira
     * excepción; sin credenciales para borrar, no hace nada.
     */
    default void borrar(ConfigImagenes cfg, java.util.List<String> urls) {
    }

    /**
     * Cómo subir el archivo desde el navegador.
     *
     * @param metodo        "POST_FORM" (multipart con {@code campos} + el archivo en {@code campoArchivo})
     *                      o "PUT" (el archivo crudo, para URLs prefirmadas como las de R2)
     * @param campoUrlFinal en la respuesta JSON del proveedor, el campo con la URL pública (POST_FORM)
     * @param urlFinal      URL pública conocida de antemano (PUT), null si la devuelve el proveedor
     */
    record TicketSubida(String proveedor, String metodo, String url, Map<String, String> campos,
                        String campoArchivo, String campoUrlFinal, String urlFinal) {
    }
}
