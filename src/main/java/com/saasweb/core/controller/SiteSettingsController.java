package com.saasweb.core.controller;

import com.saasweb.core.common.TenantContext;
import com.saasweb.core.model.SiteSettings;
import com.saasweb.core.dto.SiteSettingsDtos;

import com.saasweb.core.dto.SiteSettingsDtos.AppearanceRequest;
import com.saasweb.core.dto.SiteSettingsDtos.CloudinaryConfigRequest;
import com.saasweb.core.dto.SiteSettingsDtos.CloudinaryConfigResponse;
import com.saasweb.core.dto.SiteSettingsDtos.MailConfigRequest;
import com.saasweb.core.dto.SiteSettingsDtos.MailConfigResponse;
import com.saasweb.core.dto.SiteSettingsDtos.MercadoPagoConfigRequest;
import com.saasweb.core.dto.SiteSettingsDtos.MercadoPagoConfigResponse;
import com.saasweb.core.dto.SiteSettingsDtos.ArcaConfigRequest;
import com.saasweb.core.dto.SiteSettingsDtos.ArcaConfigResponse;
import com.saasweb.core.dto.SiteSettingsDtos.PaymentsSettingsRequest;
import com.saasweb.core.dto.SiteSettingsDtos.PlatformSettingsRequest;
import com.saasweb.core.dto.SiteSettingsDtos.SettingsResponse;
import com.saasweb.core.service.SiteSettingsService;
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

    /**
     * Credenciales de Mercado Pago DEL TENANT (Fase 13) — a diferencia de
     * Cloudinary/SMTP, no requiere SUPERADMIN: es la cuenta del propio
     * dueño de la tienda, mismo nivel que el resto de medios de pago.
     */
    @GetMapping("/api/admin/settings/mercadopago")
    @PreAuthorize("hasAuthority('PAYMENTS_MANAGE')")
    public MercadoPagoConfigResponse mercadoPagoConfig() {
        return MercadoPagoConfigResponse.from(configuracion.mp());
    }

    @PutMapping("/api/admin/settings/mercadopago")
    @PreAuthorize("hasAuthority('PAYMENTS_MANAGE')")
    public MercadoPagoConfigResponse updateMercadoPagoConfig(@Valid @RequestBody MercadoPagoConfigRequest req) {
        return MercadoPagoConfigResponse.from(configuracion.actualizarMp(req));
    }

    /**
     * Credenciales de ARCA DEL TENANT (Fase 14) — mismo criterio que
     * Mercado Pago: la propia cuenta/CUIT del dueño de la tienda, no
     * requiere SUPERADMIN.
     */
    @GetMapping("/api/admin/settings/arca")
    @PreAuthorize("hasAuthority('PAYMENTS_MANAGE')")
    public ArcaConfigResponse arcaConfig() {
        return ArcaConfigResponse.from(configuracion.arca());
    }

    @PutMapping("/api/admin/settings/arca")
    @PreAuthorize("hasAuthority('PAYMENTS_MANAGE')")
    public ArcaConfigResponse updateArcaConfig(@Valid @RequestBody ArcaConfigRequest req) {
        return ArcaConfigResponse.from(configuracion.actualizarArca(req));
    }

    /** Alerta de stock bajo por mail — la edita el admin normal de la tienda (ítem 10). */
    @GetMapping("/api/admin/settings/stock-alerts")
    @PreAuthorize("hasAuthority('PRODUCTS_MANAGE')")
    public com.saasweb.core.dto.SiteSettingsDtos.StockAlertSettingsResponse stockAlertSettings() {
        return com.saasweb.core.dto.SiteSettingsDtos.StockAlertSettingsResponse.from(service.get());
    }

    @PutMapping("/api/admin/settings/stock-alerts")
    @PreAuthorize("hasAuthority('PRODUCTS_MANAGE')")
    public com.saasweb.core.dto.SiteSettingsDtos.StockAlertSettingsResponse updateStockAlertSettings(
            @Valid @RequestBody com.saasweb.core.dto.SiteSettingsDtos.StockAlertSettingsRequest req) {
        return com.saasweb.core.dto.SiteSettingsDtos.StockAlertSettingsResponse.from(service.updateStockAlerts(req));
    }

    /**
     * Config de Cloudinary (cuenta usada para subir fotos desde el panel). Separada
     * del resto de `site_settings` porque sólo un superadmin la puede tocar — no es
     * un {@code Permission} normal, así que no se puede otorgar desde `/admin/usuarios`.
     */
    @GetMapping("/api/admin/settings/cloudinary")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public CloudinaryConfigResponse cloudinaryConfig() {
        return CloudinaryConfigResponse.from(configuracion.imagenes());
    }

    @PutMapping("/api/admin/settings/cloudinary")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public CloudinaryConfigResponse updateCloudinaryConfig(@Valid @RequestBody CloudinaryConfigRequest req) {
        return CloudinaryConfigResponse.from(configuracion.actualizarImagenes(req.cloudName(), req.uploadPreset()));
    }

    /**
     * Config de SMTP (recuperación de cuenta por mail). Igual que Cloudinary:
     * pensado para que otro sitio armado sobre esta misma base sólo tenga que
     * cargar sus propias credenciales acá, sin tocar código.
     */
    @GetMapping("/api/admin/settings/mail")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public MailConfigResponse mailConfig() {
        return MailConfigResponse.from(configuracion.mail());
    }

    @PutMapping("/api/admin/settings/mail")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public MailConfigResponse updateMailConfig(@Valid @RequestBody MailConfigRequest req) {
        return MailConfigResponse.from(
                configuracion.actualizarMail(req.host(), req.port(), req.username(), req.password(),
                        req.fromEmail(), req.fromName()));
    }
}
