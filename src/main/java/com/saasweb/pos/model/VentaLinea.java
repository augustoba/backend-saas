package com.saasweb.pos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Renglón del ticket. {@code articuloId} null = "varios": un importe suelto
 * sin artículo (no mueve stock), algo común en el mostrador.
 */
@Entity
@Table(name = "pos_venta_linea")
public class VentaLinea {

    @Id
    private String id;

    private String articuloId;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private int cantidad;

    @Column(precision = 5, scale = 2)
    private BigDecimal iva;

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getArticuloId() { return articuloId; }
    public void setArticuloId(String articuloId) { this.articuloId = articuloId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getIva() { return iva; }
    public void setIva(BigDecimal iva) { this.iva = iva; }
}
