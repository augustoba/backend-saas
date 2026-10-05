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

/** Preferencias operativas por sucursal (relevamiento §4.1, tab Preferencias). */
@Entity
@Table(name = "rs_preferencias")
public class Preferencias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sucursal_id", nullable = false, unique = true)
    private Long sucursalId;

    @Column(name = "precios_con_centavos", nullable = false)
    private boolean preciosConCentavos = true;

    @Column(name = "sonidos_activos", nullable = false)
    private boolean sonidosActivos = true;

    @Column(name = "pasos_por_defecto", nullable = false)
    private boolean pasosPorDefecto = false;

    @Column(name = "agrupar_por_producto", nullable = false)
    private boolean agruparPorProducto = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
    }

    public boolean isPreciosConCentavos() {
        return preciosConCentavos;
    }

    public void setPreciosConCentavos(boolean preciosConCentavos) {
        this.preciosConCentavos = preciosConCentavos;
    }

    public boolean isSonidosActivos() {
        return sonidosActivos;
    }

    public void setSonidosActivos(boolean sonidosActivos) {
        this.sonidosActivos = sonidosActivos;
    }

    public boolean isPasosPorDefecto() {
        return pasosPorDefecto;
    }

    public void setPasosPorDefecto(boolean pasosPorDefecto) {
        this.pasosPorDefecto = pasosPorDefecto;
    }

    public boolean isAgruparPorProducto() {
        return agruparPorProducto;
    }

    public void setAgruparPorProducto(boolean agruparPorProducto) {
        this.agruparPorProducto = agruparPorProducto;
    }
}
