package com.saasweb.ecommerce.model;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Una variante vendible de un producto: un talle ("M"), una combinación
 * ("Rojo 42"), una medida ("1/2 pulgada")… con su propio stock y, opcional,
 * su propio código de barras. Reemplaza al viejo talle embebido en el
 * producto: no hay nada de ropa acá, sirve para cualquier rubro.
 *
 * <p>Tiene identidad propia (id) para que el código de barras y las
 * referencias no se pierdan al editar el producto (ver
 * {@code Product#reemplazarVariantes}).</p>
 */
@Entity
@Table(name = "ec_variante")
public class Variante {

    @Id
    private String id = UUID.randomUUID().toString();

    /** Lo que ve el cliente: "M", "Rojo 42", "Único"… Único dentro del producto. */
    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private int stock;

    /** Código de barras propio de esta variante (el del fabricante o uno interno); opcional. */
    @Column(name = "codigo_barras", length = 64)
    private String codigoBarras;

    public Variante() {
    }

    public Variante(String nombre, int stock) {
        this.nombre = nombre;
        this.stock = stock;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    /**
     * Alias de {@link #getNombre()}: el resto del ecommerce (y la API, campo
     * {@code size}) todavía habla de "talle". Se va cuando la API y el front
     * pasen a "variante".
     */
    public String getSize() { return nombre; }
    public void setSize(String size) { this.nombre = size; }
}
