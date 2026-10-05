package com.saasweb.pos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/** Cada cambio de stock de un artículo, con el stock que quedó (historial auditable). */
@Entity
@Table(name = "pos_movimiento_stock")
public class MovimientoStock {

    public enum Motivo { VENTA, ANULACION, INGRESO, AJUSTE }

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String articuloId;

    /** Foto del nombre al momento del movimiento. */
    @Column(nullable = false, length = 200)
    private String articuloNombre;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private int stockResultante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Motivo motivo;

    /** Venta que lo originó (si aplica). */
    private String referenciaId;

    @Column(length = 500)
    private String nota;

    /** Costo unitario del ingreso de mercadería (opcional). */
    @Column(precision = 12, scale = 2)
    private BigDecimal costoUnitario;

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
    public String getArticuloId() { return articuloId; }
    public void setArticuloId(String articuloId) { this.articuloId = articuloId; }
    public String getArticuloNombre() { return articuloNombre; }
    public void setArticuloNombre(String articuloNombre) { this.articuloNombre = articuloNombre; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getStockResultante() { return stockResultante; }
    public void setStockResultante(int stockResultante) { this.stockResultante = stockResultante; }
    public Motivo getMotivo() { return motivo; }
    public void setMotivo(Motivo motivo) { this.motivo = motivo; }
    public String getReferenciaId() { return referenciaId; }
    public void setReferenciaId(String referenciaId) { this.referenciaId = referenciaId; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }
    public String getAutorDni() { return autorDni; }
    public void setAutorDni(String autorDni) { this.autorDni = autorDni; }
    public String getAutorNombre() { return autorNombre; }
    public void setAutorNombre(String autorNombre) { this.autorNombre = autorNombre; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
}
