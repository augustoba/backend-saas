package com.saasweb.restaurante.model;

import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rs_menu_online_config")
public class MenuOnlineConfig {

    public enum Layout { LISTA, CARTA, GALERIA }

    public enum Tipografia { MODERNA, CLASICA, RELAJADA, AUDAZ }

    public enum FormaLogo { CIRCULAR, CUADRADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "sucursal_id", nullable = false)
    private Long sucursalId;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "banner_url")
    private String bannerUrl;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "forma_logo", nullable = false)
    private FormaLogo formaLogo = FormaLogo.CIRCULAR;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private Layout layout = Layout.CARTA;

    @Column(name = "columnas_max", nullable = false)
    private int columnasMax = 3;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private Tipografia tipografia = Tipografia.MODERNA;

    @Column(name = "encabezado_texto", length = 2000)
    private String encabezadoTexto;

    @Column(name = "mostrar_descripciones", nullable = false)
    private boolean mostrarDescripciones = true;

    @Column(name = "mostrar_direccion", nullable = false)
    private boolean mostrarDireccion = true;

    @Column(nullable = false)
    private boolean activo = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rs_menu_online_red_social", joinColumns = @JoinColumn(name = "menu_online_config_id"))
    @OrderColumn(name = "orden")
    private List<RedSocial> redesSociales = new ArrayList<>();

    @Embeddable
    public static class RedSocial {
        private String plataforma;
        private String url;

        @Column(name = "nombre_mostrar")
        private String nombreMostrar;

        public String getPlataforma() {
            return plataforma;
        }

        public void setPlataforma(String plataforma) {
            this.plataforma = plataforma;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getNombreMostrar() {
            return nombreMostrar;
        }

        public void setNombreMostrar(String nombreMostrar) {
            this.nombreMostrar = nombreMostrar;
        }
    }

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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public FormaLogo getFormaLogo() {
        return formaLogo;
    }

    public void setFormaLogo(FormaLogo formaLogo) {
        this.formaLogo = formaLogo;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public int getColumnasMax() {
        return columnasMax;
    }

    public void setColumnasMax(int columnasMax) {
        this.columnasMax = columnasMax;
    }

    public Tipografia getTipografia() {
        return tipografia;
    }

    public void setTipografia(Tipografia tipografia) {
        this.tipografia = tipografia;
    }

    public String getEncabezadoTexto() {
        return encabezadoTexto;
    }

    public void setEncabezadoTexto(String encabezadoTexto) {
        this.encabezadoTexto = encabezadoTexto;
    }

    public boolean isMostrarDescripciones() {
        return mostrarDescripciones;
    }

    public void setMostrarDescripciones(boolean mostrarDescripciones) {
        this.mostrarDescripciones = mostrarDescripciones;
    }

    public boolean isMostrarDireccion() {
        return mostrarDireccion;
    }

    public void setMostrarDireccion(boolean mostrarDireccion) {
        this.mostrarDireccion = mostrarDireccion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<RedSocial> getRedesSociales() {
        return redesSociales;
    }

    public void setRedesSociales(List<RedSocial> redesSociales) {
        this.redesSociales = redesSociales;
    }
}
