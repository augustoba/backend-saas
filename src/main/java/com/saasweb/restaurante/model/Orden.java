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
@Table(name = "rs_orden")
public class Orden {

    public enum Tipo { MESA, MOSTRADOR, LLEVAR, DELIVERY }

    public enum Estado { EN_CURSO, CONFIRMADO, PAGADO, CANCELADO }

    /**
     * Seguimiento de preparación para la cocina de Pedidos Online
     * (Fase 18) — independiente del estado de los {@link OrdenItem} que ya
     * usa el KDS general, para no interferir con ese flujo.
     */
    public enum EstadoPreparacion { SIN_TOMAR, TOMADO, LISTO, ENTREGADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "sucursal_id", nullable = false)
    private Long sucursalId;

    @ManyToOne
    @JoinColumn(name = "arqueo_id")
    private Arqueo arqueo;

    @ManyToOne
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /** Quien abrió la orden (usuario del núcleo) y su nombre como foto. */
    @Column(name = "usuario_id")
    private String usuarioId;

    @Column(name = "usuario_nombre", length = 200)
    private String usuarioNombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.EN_CURSO;

    private String comentario;

    @Column(nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "descuento_monto", nullable = false)
    private BigDecimal descuentoMonto = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal propina = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    private Integer comensales;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion = Instant.now();

    @Column(name = "fecha_confirmacion")
    private Instant fechaConfirmacion;

    @Column(name = "fecha_pago")
    private Instant fechaPago;

    @Column(name = "fecha_tomado")
    private Instant fechaTomado;

    @Column(name = "token_sesion", unique = true)
    private String tokenSesion;

    @Column(name = "nombre_cliente_online")
    private String nombreClienteOnline;

    @Column(name = "telefono_cliente_online")
    private String telefonoClienteOnline;

    @Column(name = "direccion_entrega")
    private String direccionEntrega;

    @Column(name = "costo_envio", nullable = false)
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    @Column(name = "paso_actual", nullable = false)
    private int pasoActual = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_preparacion", nullable = false)
    private EstadoPreparacion estadoPreparacion = EstadoPreparacion.SIN_TOMAR;

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

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
    }

    public Arqueo getArqueo() {
        return arqueo;
    }

    public void setArqueo(Arqueo arqueo) {
        this.arqueo = arqueo;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getUsuario() {
        return usuarioId == null ? null : new Usuario(usuarioId, usuarioNombre, null);
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioId = usuario == null ? null : usuario.getId();
        this.usuarioNombre = usuario == null ? null : usuario.nombreCompleto();
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDescuentoMonto() {
        return descuentoMonto;
    }

    public void setDescuentoMonto(BigDecimal descuentoMonto) {
        this.descuentoMonto = descuentoMonto;
    }

    public BigDecimal getPropina() {
        return propina;
    }

    public void setPropina(BigDecimal propina) {
        this.propina = propina;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getComensales() {
        return comensales;
    }

    public void setComensales(Integer comensales) {
        this.comensales = comensales;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Instant getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public void setFechaConfirmacion(Instant fechaConfirmacion) {
        this.fechaConfirmacion = fechaConfirmacion;
    }

    public Instant getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Instant fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getTokenSesion() {
        return tokenSesion;
    }

    public void setTokenSesion(String tokenSesion) {
        this.tokenSesion = tokenSesion;
    }

    public String getNombreClienteOnline() {
        return nombreClienteOnline;
    }

    public void setNombreClienteOnline(String nombreClienteOnline) {
        this.nombreClienteOnline = nombreClienteOnline;
    }

    public String getTelefonoClienteOnline() {
        return telefonoClienteOnline;
    }

    public void setTelefonoClienteOnline(String telefonoClienteOnline) {
        this.telefonoClienteOnline = telefonoClienteOnline;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public BigDecimal getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(BigDecimal costoEnvio) {
        this.costoEnvio = costoEnvio;
    }

    public int getPasoActual() {
        return pasoActual;
    }

    public void setPasoActual(int pasoActual) {
        this.pasoActual = pasoActual;
    }

    public EstadoPreparacion getEstadoPreparacion() {
        return estadoPreparacion;
    }

    public void setEstadoPreparacion(EstadoPreparacion estadoPreparacion) {
        this.estadoPreparacion = estadoPreparacion;
    }

    public Instant getFechaTomado() {
        return fechaTomado;
    }

    public void setFechaTomado(Instant fechaTomado) {
        this.fechaTomado = fechaTomado;
    }
}
