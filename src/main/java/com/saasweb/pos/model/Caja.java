package com.saasweb.pos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * La caja es el turno: se abre con el efectivo inicial y se cierra contando el
 * efectivo (arqueo). Una sola abierta por tienda. El resumen por medio de pago
 * se calcula de sus ventas (que no cambian después del cierre).
 */
@Entity(name = "PosCaja")
@Table(name = "pos_caja")
public class Caja {

    public enum Estado { ABIERTA, CERRADA }

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    /** Correlativo por tienda: "Caja #12". */
    @Column(nullable = false)
    private int numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado = Estado.ABIERTA;

    @Column(nullable = false)
    private Instant abiertaEn = Instant.now();

    @Column(length = 20)
    private String abiertaPorDni;

    @Column(length = 200)
    private String abiertaPorNombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoInicial = BigDecimal.ZERO;

    private Instant cerradaEn;

    @Column(length = 20)
    private String cerradaPorDni;

    @Column(length = 200)
    private String cerradaPorNombre;

    /** Lo que tendría que haber en efectivo al cerrar (inicial + ventas en efectivo + ingresos − egresos). */
    @Column(precision = 12, scale = 2)
    private BigDecimal efectivoEsperado;

    /** Lo que se contó al cerrar. */
    @Column(precision = 12, scale = 2)
    private BigDecimal efectivoContado;

    /** contado − esperado: positivo sobra, negativo falta. */
    @Column(precision = 12, scale = 2)
    private BigDecimal diferencia;

    @Column(length = 500)
    private String notaCierre;

    public boolean abierta() {
        return estado == Estado.ABIERTA;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public Instant getAbiertaEn() { return abiertaEn; }
    public void setAbiertaEn(Instant abiertaEn) { this.abiertaEn = abiertaEn; }
    public String getAbiertaPorDni() { return abiertaPorDni; }
    public void setAbiertaPorDni(String abiertaPorDni) { this.abiertaPorDni = abiertaPorDni; }
    public String getAbiertaPorNombre() { return abiertaPorNombre; }
    public void setAbiertaPorNombre(String abiertaPorNombre) { this.abiertaPorNombre = abiertaPorNombre; }
    public BigDecimal getMontoInicial() { return montoInicial; }
    public void setMontoInicial(BigDecimal montoInicial) { this.montoInicial = montoInicial; }
    public Instant getCerradaEn() { return cerradaEn; }
    public void setCerradaEn(Instant cerradaEn) { this.cerradaEn = cerradaEn; }
    public String getCerradaPorDni() { return cerradaPorDni; }
    public void setCerradaPorDni(String cerradaPorDni) { this.cerradaPorDni = cerradaPorDni; }
    public String getCerradaPorNombre() { return cerradaPorNombre; }
    public void setCerradaPorNombre(String cerradaPorNombre) { this.cerradaPorNombre = cerradaPorNombre; }
    public BigDecimal getEfectivoEsperado() { return efectivoEsperado; }
    public void setEfectivoEsperado(BigDecimal efectivoEsperado) { this.efectivoEsperado = efectivoEsperado; }
    public BigDecimal getEfectivoContado() { return efectivoContado; }
    public void setEfectivoContado(BigDecimal efectivoContado) { this.efectivoContado = efectivoContado; }
    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }
    public String getNotaCierre() { return notaCierre; }
    public void setNotaCierre(String notaCierre) { this.notaCierre = notaCierre; }
}
