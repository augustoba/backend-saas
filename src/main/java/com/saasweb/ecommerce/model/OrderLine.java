package com.saasweb.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ec_pedido_linea")
public class OrderLine {

    @Id
    private String id;

    @Column(nullable = false)
    private String productId;

    /** Nombre del producto al momento del pedido (por si luego se edita/borra). */
    @Column(nullable = false)
    private String productName;

    @Column(name = "size_value", nullable = false)
    private String size;

    /**
     * Variante vendida (ver {@link Variante}). {@code size} queda como foto del
     * nombre al momento de la venta, por si la variante después se renombra o se borra.
     */
    @Column(name = "variante_id")
    private String varianteId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Costo del producto congelado al CONFIRMAR el pedido (no al crearlo) —
     * para que la ganancia histórica no cambie si después se actualiza
     * {@code Product.costPrice}. {@code null} = el producto no tenía costo
     * cargado en ese momento (no se puede saber el margen de esta línea).
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal costPrice;

    /**
     * {@code true} sólo en líneas completadas por el backfill de pedidos
     * históricos (con el costo ACTUAL del producto, no el real de esa venta)
     * — ver {@code DataSeeder.backfillOrderLineCosts}.
     */
    @Column(nullable = false)
    private boolean costEstimated = false;

    /** PENDIENTE hasta que se entrega o se cancela (entrega parcial, ver {@link OrderLineStatus}). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderLineStatus status = OrderLineStatus.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getVarianteId() {
        return varianteId;
    }

    public void setVarianteId(String varianteId) {
        this.varianteId = varianteId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public boolean isCostEstimated() {
        return costEstimated;
    }

    public void setCostEstimated(boolean costEstimated) {
        this.costEstimated = costEstimated;
    }

    public OrderLineStatus getStatus() {
        return status;
    }

    public void setStatus(OrderLineStatus status) {
        this.status = status;
    }

    /** true si se entregó: es lo que cuenta como vendido (métricas, factura, más vendidos). */
    public boolean vendida() {
        return status == OrderLineStatus.ENTREGADA;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
