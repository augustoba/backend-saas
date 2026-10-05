package com.saasweb.pos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Un gasto del negocio (alquiler, luz, mercadería, sueldos). Si se pagó con
 * el efectivo de la caja abierta, genera el egreso en esa caja.
 */
@Entity(name = "PosGasto")
@Table(name = "pos_gasto")
public class Gasto {

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 200)
    private String concepto;

    /** Rubro libre para agrupar (Servicios, Mercadería, Sueldos...). */
    @Column(length = 100)
    private String categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    private String proveedorId;

    /** Egreso que generó en la caja (si se pagó de la caja). */
    private String movimientoCajaId;

    @Column(length = 200)
    private String autorNombre;

    @Column(nullable = false)
    private Instant creadoEn = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getProveedorId() { return proveedorId; }
    public void setProveedorId(String proveedorId) { this.proveedorId = proveedorId; }
    public String getMovimientoCajaId() { return movimientoCajaId; }
    public void setMovimientoCajaId(String movimientoCajaId) { this.movimientoCajaId = movimientoCajaId; }
    public String getAutorNombre() { return autorNombre; }
    public void setAutorNombre(String autorNombre) { this.autorNombre = autorNombre; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
