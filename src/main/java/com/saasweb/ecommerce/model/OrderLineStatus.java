package com.saasweb.ecommerce.model;

/**
 * Estado de una línea de pedido: permite entregar o cancelar parte de un
 * pedido en vez de todo junto (ej: de 3 productos, 2 tenían stock y se
 * entregaron, el tercero no llegó y se cancela solo). Ver
 * {@code OrderService#confirmLines} / {@code #cancelLines}. Traído del
 * ecommerce de Ruth.
 */
public enum OrderLineStatus {
    /** Todavía no se entregó ni se canceló: el pedido sigue esperando esta línea. */
    PENDIENTE,
    /** Se descontó stock y se le entregó al cliente. */
    ENTREGADA,
    /** No se va a entregar (sin stock, el cliente desistió de ese ítem, etc). */
    CANCELADA
}
