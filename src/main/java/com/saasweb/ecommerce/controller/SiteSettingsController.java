package com.saasweb.ecommerce.controller;

import com.saasweb.core.common.TenantContext;
import com.saasweb.ecommerce.model.SiteSettings;
import com.saasweb.ecommerce.dto.SiteSettingsDtos;

import com.saasweb.ecommerce.dto.SiteSettingsDtos.AppearanceRequest;
import com.saasweb.ecommerce.dto.SiteSettingsDtos.PaymentsSettingsRequest;
import com.saasweb.ecommerce.dto.SiteSettingsDtos.PlatformSettingsRequest;
import com.saasweb.ecommerce.dto.SiteSettingsDtos.SettingsResponse;
import com.saasweb.ecommerce.service.SiteSettingsService;
import com.saasweb.core.model.Modules;
import com.saasweb.core.service.PlanService;
import com.saasweb.core.service.ConfiguracionService;
import com.saasweb.core.service.TenantService;
import com.saasweb.core.service.ArcaInvoiceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class SiteSettingsController {

    private final SiteSettingsService service;
    private final PlanService planService;
    private final TenantService tenantService;
    private final ConfiguracionService configuracion;
    private final ArcaInvoiceService arcaInvoiceService;

    public SiteSettingsController(SiteSettingsService service, PlanService planService,
                                   ArcaInvoiceService arcaInvoiceService, TenantService tenantService,
                                   ConfiguracionService configuracion) {
        this.configuracion = configuracion;
        this.tenantService = tenantService;
        this.service = service;
        this.planService = planService;
        this.arcaInvoiceService = arcaInvoiceService;
    }

    /** true si el plan del tenant actual habilita ese módulo (ver {@link Modules}). */
    private boolean socialShareEnabled() {
        var plan = planService.getCurrent();
        return plan != null && plan.hasModule(Modules.SOCIAL_SHARE);
    }

    /** true sólo si el plan lo habilita Y el tenant activó el checkout Y ya cargó su Access Token. */
    private boolean mercadoPagoAvailable() {
        var plan = planService.getCurrent();
        return plan != null && plan.hasModule(Modules.MERCADOPAGO) && configuracion.mp().listo();
    }

    private SettingsResponse toResponse(SiteSettings s) {
        return SettingsResponse.from(s, configuracion.identidad(), configuracion.imagenes(), configuracion.arca(),
                socialShareEnabled(), mercadoPagoAvailable(),
                tenantService.sistemaDe(TenantContext.requireTenantId()), arcaInvoiceService.isAvailable());
    }

    /** Público: datos del local para el header, footer y el link de WhatsApp. */
    @GetMapping("/api/settings")
    public SettingsResponse publicSettings() {
        return toResponse(service.get());
    }

    @GetMapping("/api/admin/settings")
    @PreAuthorize("hasAnyAuthority('PLATFORM_SETTINGS_MANAGE', 'PAYMENTS_MANAGE')")
    public SettingsResponse adminSettings() {
        return toResponse(service.get());
    }

    /** Identidad, logo, WhatsApp, redes, textos. Sólo superadmin. */
    @PutMapping("/api/admin/settings/platform")
    @PreAuthorize("hasAuthority('PLATFORM_SETTINGS_MANAGE')")
    public SettingsResponse updatePlatform(@Valid @RequestBody PlatformSettingsRequest req) {
        return toResponse(service.updatePlatform(req));
    }

    /** Diseño de página + color de marca (ver PLAN_SAAS.md Fase 10). Mismo nivel que identidad/logo. */
    @PutMapping("/api/admin/settings/appearance")
    @PreAuthorize("hasAuthority('PLATFORM_SETTINGS_MANAGE')")
    public SettingsResponse updateAppearance(@Valid @RequestBody AppearanceRequest req) {
        return toResponse(service.updateAppearance(req));
    }

    /** Medios de pago. Lo edita el admin normal de la tienda. */
    @PutMapping("/api/admin/settings/payments")
    @PreAuthorize("hasAuthority('PAYMENTS_MANAGE')")
    public SettingsResponse updatePayments(@Valid @RequestBody PaymentsSettingsRequest req) {
        return toResponse(service.updatePayments(req));
    }

    /** Alerta de stock bajo por mail — la edita el admin normal de la tienda (ítem 10). */
    @GetMapping("/api/admin/settings/stock-alerts")
    @PreAuthorize("hasAuthority('PRODUCTS_MANAGE')")
    public com.saasweb.ecommerce.dto.SiteSettingsDtos.StockAlertSettingsResponse stockAlertSettings() {
        return com.saasweb.ecommerce.dto.SiteSettingsDtos.StockAlertSettingsResponse.from(service.get());
    }

    @PutMapping("/api/admin/settings/stock-alerts")
    @PreAuthorize("hasAuthority('PRODUCTS_MANAGE')")
    public com.saasweb.ecommerce.dto.SiteSettingsDtos.StockAlertSettingsResponse updateStockAlertSettings(
            @Valid @RequestBody com.saasweb.ecommerce.dto.SiteSettingsDtos.StockAlertSettingsRequest req) {
        return com.saasweb.ecommerce.dto.SiteSettingsDtos.StockAlertSettingsResponse.from(service.updateStockAlerts(req));
    }

}
