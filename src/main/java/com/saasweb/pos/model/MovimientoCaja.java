package com.saasweb.pos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/** Ingreso o egreso manual de efectivo durante el turno (retiro, cambio, pago a un proveedor). */
@Entity(name = "PosMovimientoCaja")
@Table(name = "pos_movimiento_caja")
public class MovimientoCaja {

    public enum Tipo { INGRESO, EGRESO }

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String cajaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 300)
    private String motivo;

    /** Gasto que lo generó (pagado de la caja), si aplica. */
    private String gastoId;

    @Column(length = 20)
    private String autorDni;

    @Column(length = 200)
    private String autorNombre;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCajaId() { return cajaId; }
    public void setCajaId(String cajaId) { this.cajaId = cajaId; }
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getGastoId() { return gastoId; }
    public void setGastoId(String gastoId) { this.gastoId = gastoId; }
    public String getAutorDni() { return autorDni; }
    public void setAutorDni(String autorDni) { this.autorDni = autorDni; }
    public String getAutorNombre() { return autorNombre; }
    public void setAutorNombre(String autorNombre) { this.autorNombre = autorNombre; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
}
