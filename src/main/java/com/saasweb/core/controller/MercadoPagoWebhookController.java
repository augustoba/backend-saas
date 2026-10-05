package com.saasweb.core.controller;

import com.saasweb.core.common.TenantContext;
import com.saasweb.core.service.CobroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Notificaciones de pago de Mercado Pago — público, sin JWT. El tenant viene
 * del query param {@code tenantId} que agrega {@code CobroService} a la
 * {@code notification_url}, nunca del host (Mercado Pago no llama desde el
 * dominio de la tienda).
 *
 * <p>Siempre responde 200: Mercado Pago reintenta ante cualquier otra cosa y
 * reintentar no arregla un pedido que no se pudo confirmar (eso queda anotado
 * en el documento del sistema dueño, ver {@code CobroActualizado}).</p>
 */
@RestController
public class MercadoPagoWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoWebhookController.class);

    private final CobroService cobros;

    public MercadoPagoWebhookController(CobroService cobros) {
        this.cobros = cobros;
    }

    @PostMapping("/api/webhooks/mercadopago")
    public ResponseEntity<Void> receive(
            @RequestParam(required = false) String tenantId,
            @RequestParam(name = "data.id", required = false) String dataIdParam,
            @RequestParam(name = "type", required = false) String typeParam,
            @RequestBody(required = false) Map<String, Object> body) {
        if (tenantId == null || tenantId.isBlank()) return ResponseEntity.ok().build();
        String type = typeParam != null ? typeParam : topicFromBody(body);
        String paymentId = dataIdParam != null ? dataIdParam : paymentIdFromBody(body);
        if (!"payment".equals(type) || paymentId == null || paymentId.isBlank()) return ResponseEntity.ok().build();
        String anterior = TenantContext.getTenantId();
        try {
            TenantContext.set(tenantId);
            cobros.procesarNotificacion(tenantId, paymentId);
        } catch (RuntimeException e) {
            log.error("No se pudo procesar el pago {} del tenant {}: {}", paymentId, tenantId, e.getMessage());
        } finally {
            TenantContext.set(anterior);
        }
        return ResponseEntity.ok().build();
    }

    private static String topicFromBody(Map<String, Object> body) {
        if (body == null) return null;
        Object t = body.get("type");
        return t != null ? String.valueOf(t) : null;
    }

    private static String paymentIdFromBody(Map<String, Object> body) {
        if (body == null) return null;
        Object data = body.get("data");
        if (data instanceof Map<?, ?> m && m.get("id") != null) return String.valueOf(m.get("id"));
        return null;
    }
}
