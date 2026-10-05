package com.saasweb.restaurante.model;

import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "rs_arqueo")
public class Arqueo {

    public enum Estado { ABIERTO, CERRADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    @Column(name = "usuario_apertura_id", nullable = false)
    private String usuarioAperturaId;

    @Column(name = "usuario_cierre_id")
    private String usuarioCierreId;

    @Column(name = "fecha_apertura", nullable = false)
    private Instant fechaApertura = Instant.now();

    @Column(name = "fecha_cierre")
    private Instant fechaCierre;

    @Column(name = "monto_apertura", nullable = false)
    private BigDecimal montoApertura;

    @Column(name = "monto_cierre_declarado")
    private BigDecimal montoCierreDeclarado;

    @Column(name = "monto_cierre_sistema")
    private BigDecimal montoCierreSistema;

    private BigDecimal diferencia;

    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ABIERTO;

    @Column(name = "total_ventas", nullable = false)
    private BigDecimal totalVentas = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Caja getCaja() {
        return caja;
    }

    public void setCaja(Caja caja) {
        this.caja = caja;
    }

    public String getUsuarioAperturaId() {
        return usuarioAperturaId;
    }

    public void setUsuarioAperturaId(String usuarioAperturaId) {
        this.usuarioAperturaId = usuarioAperturaId;
    }

    public String getUsuarioCierreId() {
        return usuarioCierreId;
    }

    public void setUsuarioCierreId(String usuarioCierreId) {
        this.usuarioCierreId = usuarioCierreId;
    }

    public Instant getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(Instant fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public Instant getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(Instant fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public BigDecimal getMontoApertura() {
        return montoApertura;
    }

    public void setMontoApertura(BigDecimal montoApertura) {
        this.montoApertura = montoApertura;
    }

    public BigDecimal getMontoCierreDeclarado() {
        return montoCierreDeclarado;
    }

    public void setMontoCierreDeclarado(BigDecimal montoCierreDeclarado) {
        this.montoCierreDeclarado = montoCierreDeclarado;
    }

    public BigDecimal getMontoCierreSistema() {
        return montoCierreSistema;
    }

    public void setMontoCierreSistema(BigDecimal montoCierreSistema) {
        this.montoCierreSistema = montoCierreSistema;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public BigDecimal getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }
}
