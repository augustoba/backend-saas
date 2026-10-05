package com.saasweb.restaurante.model;

/**
 * Quien opera (mozo, cajero): el usuario del núcleo visto desde el restaurante.
 * No es una entidad: el restaurante guarda su id (y el nombre como foto donde
 * hace falta mostrarlo). Lo inyecta {@code UsuarioActualResolver} en los controllers.
 */
public final class Usuario {

    private final String id;
    private final String nombre;
    private final String apellido;

    public Usuario(String id, String nombre, String apellido) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }

    public String nombreCompleto() {
        return ((nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido)).trim();
    }
}
