package com.saasweb.pos.model;

import com.saasweb.core.model.Comprobante;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Un ticket del mostrador. Las líneas guardan la foto de nombre, precio e IVA del artículo. */
@Entity
@Table(name = "pos_venta")
public class Venta {

    public enum MedioPago { EFECTIVO, TARJETA, TRANSFERENCIA, QR }

    public enum Estado { COMPLETADA, ANULADA }

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    /** Correlativo por tienda: "V-0001". */
    @Column(nullable = false)
    private int numero;

    @Column(nullable = false)
    private String cajaId;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    @Column(length = 20)
    private String cajeroDni;

    @Column(length = 200)
    private String cajeroNombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedioPago medioPago;

    /** Sólo en efectivo: con cuánto pagó (opcional) y el vuelto. */
    @Column(precision = 12, scale = 2)
    private BigDecimal montoEntregado;

    @Column(precision = 12, scale = 2)
    private BigDecimal vuelto;

    /** Nº de ticket del posnet o quién transfirió (opcional). */
    @Column(length = 200)
    private String referencia;

    @Column(length = 20)
    private String cuitComprador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado = Estado.COMPLETADA;

    private Instant anuladaEn;

    @Column(length = 200)
    private String anuladaPorNombre;

    @Column(length = 300)
    private String motivoAnulacion;

    /** Factura de ARCA (último intento). null = ticket interno. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_id")
    private Comprobante factura;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "venta_id", nullable = false)
    @OrderColumn(name = "posicion")
    private List<VentaLinea> lineas = new ArrayList<>();

    public String codigo() {
        return String.format("V-%04d", numero);
    }

    public boolean facturada() {
        return factura != null && factura.aprobado();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getCajaId() { return cajaId; }
    public void setCajaId(String cajaId) { this.cajaId = cajaId; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
    public String getCajeroDni() { return cajeroDni; }
    public void setCajeroDni(String cajeroDni) { this.cajeroDni = cajeroDni; }
    public String getCajeroNombre() { return cajeroNombre; }
    public void setCajeroNombre(String cajeroNombre) { this.cajeroNombre = cajeroNombre; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public MedioPago getMedioPago() { return medioPago; }
    public void setMedioPago(MedioPago medioPago) { this.medioPago = medioPago; }
    public BigDecimal getMontoEntregado() { return montoEntregado; }
    public void setMontoEntregado(BigDecimal montoEntregado) { this.montoEntregado = montoEntregado; }
    public BigDecimal getVuelto() { return vuelto; }
    public void setVuelto(BigDecimal vuelto) { this.vuelto = vuelto; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getCuitComprador() { return cuitComprador; }
    public void setCuitComprador(String cuitComprador) { this.cuitComprador = cuitComprador; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public Instant getAnuladaEn() { return anuladaEn; }
    public void setAnuladaEn(Instant anuladaEn) { this.anuladaEn = anuladaEn; }
    public String getAnuladaPorNombre() { return anuladaPorNombre; }
    public void setAnuladaPorNombre(String anuladaPorNombre) { this.anuladaPorNombre = anuladaPorNombre; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String motivoAnulacion) { this.motivoAnulacion = motivoAnulacion; }
    public Comprobante getFactura() { return factura; }
    public void setFactura(Comprobante factura) { this.factura = factura; }
    public List<VentaLinea> getLineas() { return lineas; }
}
