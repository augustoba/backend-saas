package com.saasweb.pos.model;

import jakarta.persistence.*;

/** Rubro del kiosco para ordenar artículos (bebidas, golosinas, cigarrillos...). */
@Entity
@Table(name = "pos_categoria")
public class Categoria {

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private int posicion = 0;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }
}
