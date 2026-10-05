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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rs_producto")
public class Producto {

    public enum Tipo { INGREDIENTE, VENDIBLE, COMBO, SERVICIO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo = Tipo.VENDIBLE;

    private BigDecimal costo;

    private BigDecimal precio;

    private String unidad;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "es_recomendacion", nullable = false)
    private boolean esRecomendacion = false;

    @Column(name = "es_favorito", nullable = false)
    private boolean esFavorito = false;

    @Column(nullable = false)
    private boolean disponible = true;

    @Column(name = "disponible_online", nullable = false)
    private boolean disponibleOnline = true;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "controla_stock", nullable = false)
    private boolean controlaStock = false;

    @Column(name = "stock_actual")
    private Integer stockActual;

    @Column(nullable = false)
    private int likes = 0;

    @Column(nullable = false)
    private int vistas = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rs_producto_grupo_modificador",
            joinColumns = @JoinColumn(name = "producto_id"),
            inverseJoinColumns = @JoinColumn(name = "grupo_modificador_id"))
    private Set<GrupoModificador> gruposModificadores = new HashSet<>();

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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public boolean isEsRecomendacion() {
        return esRecomendacion;
    }

    public void setEsRecomendacion(boolean esRecomendacion) {
        this.esRecomendacion = esRecomendacion;
    }

    public boolean isEsFavorito() {
        return esFavorito;
    }

    public void setEsFavorito(boolean esFavorito) {
        this.esFavorito = esFavorito;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public boolean isDisponibleOnline() {
        return disponibleOnline;
    }

    public void setDisponibleOnline(boolean disponibleOnline) {
        this.disponibleOnline = disponibleOnline;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isControlaStock() {
        return controlaStock;
    }

    public void setControlaStock(boolean controlaStock) {
        this.controlaStock = controlaStock;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getVistas() {
        return vistas;
    }

    public void setVistas(int vistas) {
        this.vistas = vistas;
    }

    public Set<GrupoModificador> getGruposModificadores() {
        return gruposModificadores;
    }

    public void setGruposModificadores(Set<GrupoModificador> gruposModificadores) {
        this.gruposModificadores = gruposModificadores;
    }
}
