package com.saasweb.ecommerce.service;

import com.saasweb.ecommerce.model.Order;
import com.saasweb.ecommerce.repository.OrderRepository;
import com.saasweb.ecommerce.model.OrderStatus;
import com.saasweb.ecommerce.model.SaleChannel;

import com.saasweb.core.common.TenantContext;
import com.saasweb.core.service.ConfiguracionService;
import com.saasweb.core.model.Tenant;
import com.saasweb.core.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Reintenta facturar con ARCA las ventas presenciales que quedaron en ticket
 * interno por un error de ARCA (rechazo, falla de conexión) — mismo patrón
 * tenant-loop que {@code MarketingCampaignScheduler}. No toca pedidos que
 * nunca tuvieron ARCA configurado a propósito, ni cobra ni descuenta stock de
 * nuevo (ver {@code OrderService.retryInvoicing}, ya usado por el botón
 * manual del panel — ítem 5).
 */
@Component
public class ArcaRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(ArcaRetryScheduler.class);

    private final TenantRepository tenants;
    private final ConfiguracionService configuracion;
    private final OrderRepository orderRepo;
    private final OrderService orderService;

    public ArcaRetryScheduler(TenantRepository tenants, ConfiguracionService configuracion,
                              OrderRepository orderRepo, OrderService orderService) {
        this.tenants = tenants;
        this.configuracion = configuracion;
        this.orderRepo = orderRepo;
        this.orderService = orderService;
    }

    /** Cada 6 horas. Cron fijo (cambiarlo requiere redeploy). */
    @Scheduled(cron = "0 0 */6 * * *")
    public void runPeriodically() {
        for (Tenant t : tenants.findAll()) {
            if (!t.isActive()) continue;
            TenantContext.set(t.getId());
            try {
                runFor(t);
            } finally {
                TenantContext.clear();
            }
        }
    }

    private void runFor(Tenant t) {
        if (!"FACTURA_ARCA".equals(configuracion.arca().getInvoiceMode())) return;
        for (Order o : orderRepo.findByTenantIdAndChannelAndStatusAndFacturaErrorIsNotNull(
                t.getId(), SaleChannel.LOCAL, OrderStatus.PROCESADO)) {
            try {
                orderService.retryInvoicing(o.getId());
                log.info("ARCA: reintento automático OK para el pedido '{}' (tenant '{}').", o.getCode(), t.getSlug());
            } catch (Exception e) {
                log.debug("ARCA: reintento automático sigue fallando para '{}': {}", o.getCode(), e.getMessage());
            }
        }
    }
}
