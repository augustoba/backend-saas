package com.saasweb.pos.model;

import jakarta.persistence.*;

import java.time.Instant;

/** A quién le compra mercadería el kiosco. */
@Entity(name = "PosProveedor")
@Table(name = "pos_proveedor")
public class Proveedor {

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 50)
    private String telefono;

    @Column(length = 1000)
    private String nota;

    @Column(nullable = false)
    private Instant creadoEn = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
