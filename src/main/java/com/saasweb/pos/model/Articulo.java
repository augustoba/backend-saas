package com.saasweb.pos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Lo que se vende en el mostrador: un precio, sin talles ni variantes (eso es
 * del ecommerce). El stock puede quedar negativo: vender sin stock registrado
 * está permitido (ver spec §5.1).
 */
@Entity(name = "PosArticulo")
@Table(name = "pos_articulo")
public class Articulo {

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false, length = 200)
    private String nombre;

    /** Código de barras (EAN/UPC o propio). Opcional, único en la tienda. */
    @Column(length = 64)
    private String codigoBarras;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(precision = 12, scale = 2)
    private BigDecimal costo;

    /** Alícuota de IVA (21, 10.5, 0...). null = la general (21). La usa la factura de ARCA. */
    @Column(precision = 5, scale = 2)
    private BigDecimal iva;

    @Column(nullable = false)
    private int stock = 0;

    /** Aviso de stock bajo por artículo. null = sin aviso. */
    private Integer umbralStock;

    private String categoriaId;

    /** Archivado: no aparece para vender, pero sigue en las ventas viejas. */
    @Column(nullable = false)
    private boolean archivado = false;

    @Column(nullable = false)
    private Instant creadoEn = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
    public BigDecimal getIva() { return iva; }
    public void setIva(BigDecimal iva) { this.iva = iva; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public Integer getUmbralStock() { return umbralStock; }
    public void setUmbralStock(Integer umbralStock) { this.umbralStock = umbralStock; }
    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }
    public boolean isArchivado() { return archivado; }
    public void setArchivado(boolean archivado) { this.archivado = archivado; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }

    public boolean stockBajo() {
        return umbralStock != null && stock <= umbralStock;
    }
}
