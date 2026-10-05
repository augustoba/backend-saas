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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rs_medio_pago")
public class MedioPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "es_efectivo", nullable = false)
    private boolean esEfectivo = false;

    @Column(name = "es_cuenta_corriente", nullable = false)
    private boolean esCuentaCorriente = false;

    @Column(name = "requiere_referencia", nullable = false)
    private boolean requiereReferencia = false;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private int orden = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEsEfectivo() {
        return esEfectivo;
    }

    public void setEsEfectivo(boolean esEfectivo) {
        this.esEfectivo = esEfectivo;
    }

    public boolean isEsCuentaCorriente() {
        return esCuentaCorriente;
    }

    public void setEsCuentaCorriente(boolean esCuentaCorriente) {
        this.esCuentaCorriente = esCuentaCorriente;
    }

    public boolean isRequiereReferencia() {
        return requiereReferencia;
    }

    public void setRequiereReferencia(boolean requiereReferencia) {
        this.requiereReferencia = requiereReferencia;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}
