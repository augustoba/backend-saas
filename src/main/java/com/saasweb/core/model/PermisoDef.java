package com.saasweb.core.model;

/**
 * Un permiso del panel: la clave (lo que chequea {@code @PreAuthorize}) y el
 * texto que ve el admin al armar un rol. {@code soloSuperadmin} = no se le da
 * al "Administrador" de una tienda (configuración de la plataforma).
 */
public record PermisoDef(String clave, String etiqueta, boolean soloSuperadmin) {

    public static PermisoDef de(String clave, String etiqueta) {
        return new PermisoDef(clave, etiqueta, false);
    }
}
