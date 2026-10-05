package com.saasweb.core.service;

import com.saasweb.core.model.EstadoCobro;
import com.saasweb.core.model.Sistema;

/**
 * Evento que publica {@link CobroService} cuando Mercado Pago avisa que un
 * cobro cambió (aprobado o rechazado). El sistema dueño del origen lo escucha
 * ({@code @EventListener}) y actúa sobre su documento (ej: el ecommerce
 * confirma el pedido). Corre en el mismo hilo, con el tenant ya resuelto.
 */
public record CobroActualizado(String tenantId, Sistema sistema, String origenId, EstadoCobro estado,
                               String paymentId) {
}
