package com.saasweb.core.service;

import com.saasweb.core.dto.SiteSettingsDtos;

import com.saasweb.core.common.TenantContext;
import com.saasweb.core.dto.SiteSettingsDtos.PaymentsSettingsRequest;
import com.saasweb.core.dto.SiteSettingsDtos.PlatformSettingsRequest;
import com.saasweb.core.model.SiteSettings;
import com.saasweb.core.repository.SiteSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Configuración de la tienda online: diseño, colores, medios de pago manuales,
 * textos de WhatsApp/ayuda y alerta de stock. Es del ecommerce (en la Etapa 2
 * pasa a {@code ec_config_tienda}). La identidad y las credenciales (ARCA,
 * Mercado Pago, mail, imágenes) son del núcleo: {@link ConfiguracionService}.
 */
@Service
@Transactional
public class SiteSettingsService {

    private final SiteSettingsRepository repo;
    private final ConfiguracionService configuracion;

    public SiteSettingsService(SiteSettingsRepository repo, ConfiguracionService configuracion) {
        this.repo = repo;
        this.configuracion = configuracion;
    }

    /**
     * Crea la configuración de un tenant nuevo con su nombre, diseño y logo (sin
     * la marca de la tienda piloto). La llama {@code TenantProvisioningService}
     * antes de que nadie pida {@link #get()} para ese tenant.
     */
    public SiteSettings createFor(String tenantId, String storeName, String theme, String layout, String logoUrl) {
        configuracion.crearPara(tenantId, storeName, logoUrl);
        SiteSettings s = new SiteSettings();
        s.setId(tenantId);
        s.setTheme(theme);
        s.setLayout(layout);
        return repo.save(s);
    }

    /**
     * Todos los campos opcionales que junta el asistente "Crear tienda" ANTES de
     * crear nada (ver PLAN_SAAS.md Fase 10). Cada uno, si no es null, pisa el
     * valor por defecto de la tienda recién creada.
     */
    public record OnboardingExtras(String brandColor, String headerColor, String footerColor, String textColor,
                                    String pageBackgroundColor, String whatsappNumber, String instagram,
                                    String facebookUrl, String logoUrl, String logoShape) {
        public boolean isEmpty() {
            return brandColor == null && headerColor == null && footerColor == null && textColor == null
                    && pageBackgroundColor == null && whatsappNumber == null && instagram == null
                    && facebookUrl == null && logoUrl == null && logoShape == null;
        }
    }

    /**
     * Aplica los extras del asistente directo por id de tenant: durante el alta
     * la request todavía está en el contexto del superadmin, no en el de la
     * tienda recién creada.
     */
    public void applyOnboardingExtras(String tenantId, OnboardingExtras extras) {
        if (extras.isEmpty()) return;
        configuracion.aplicarIdentidadInicial(tenantId, extras.brandColor(), extras.whatsappNumber(),
                extras.instagram(), extras.facebookUrl(), extras.logoUrl(), extras.logoShape());
        SiteSettings s = repo.findById(tenantId).orElseThrow();
        if (extras.headerColor() != null) s.setHeaderColor(extras.headerColor());
        if (extras.footerColor() != null) s.setFooterColor(extras.footerColor());
        if (extras.textColor() != null) s.setTextColor(extras.textColor());
        if (extras.pageBackgroundColor() != null) s.setPageBackgroundColor(extras.pageBackgroundColor());
        repo.save(s);
    }

    /** Devuelve la configuración del tenant actual; si no existe, la crea con los valores por defecto. */
    public SiteSettings get() {
        String tenantId = TenantContext.getTenantId();
        return repo.findById(tenantId).orElseGet(() -> {
            SiteSettings s = defaults();
            s.setId(tenantId);
            return repo.save(s);
        });
    }

    /** Textos por defecto del mensaje de pedido de WhatsApp (si no se personalizan). */
    public static final String DEFAULT_WHATSAPP_INTRO = "¡Hola! Quiero hacer un pedido en *{tienda}* 🧸";
    public static final String DEFAULT_WHATSAPP_CLOSING =
            "Quedo atento/a a que me pases el alias o el link de Mercado Pago para coordinar el pago. ¡Gracias!";

    /** Formulario "Identidad" del panel: la identidad va al núcleo, los textos de la tienda quedan acá. */
    public SiteSettings updatePlatform(PlatformSettingsRequest req) {
        configuracion.actualizarIdentidad(req);
        SiteSettings s = get();
        s.setWhatsappIntro(blankToNull(req.whatsappIntro()));
        s.setWhatsappClosing(blankToNull(req.whatsappClosing()));
        s.setHelpText(blankToNull(req.helpText()));
        s.setFaqText(blankToNull(req.faqText()));
        return repo.save(s);
    }

    /** Diseño de página + color de marca (ver PLAN_SAAS.md Fase 10). */
    public SiteSettings updateAppearance(SiteSettingsDtos.AppearanceRequest req) {
        configuracion.actualizarColorDeMarca(req.brandColor());
        SiteSettings s = get();
        s.setLayout(blankToNull(req.layout()));
        s.setHeaderColor(blankToNull(req.headerColor()));
        s.setFooterColor(blankToNull(req.footerColor()));
        s.setTextColor(blankToNull(req.textColor()));
        s.setPageBackgroundColor(blankToNull(req.pageBackgroundColor()));
        return repo.save(s);
    }

    public SiteSettings updatePayments(PaymentsSettingsRequest req) {
        SiteSettings s = get();
        s.setPaymentTransferEnabled(Boolean.TRUE.equals(req.paymentTransferEnabled()));
        s.setPaymentTransferAlias(blankToNull(req.paymentTransferAlias()));
        s.setPaymentQrTransferEnabled(Boolean.TRUE.equals(req.paymentQrTransferEnabled()));
        s.setPaymentQrTransferImage(blankToNull(req.paymentQrTransferImage()));
        s.setPaymentQrCardEnabled(Boolean.TRUE.equals(req.paymentQrCardEnabled()));
        s.setPaymentQrCardImage(blankToNull(req.paymentQrCardImage()));
        s.setPaymentCardLink(blankToNull(req.paymentCardLink()));
        s.setPaymentCashEnabled(Boolean.TRUE.equals(req.paymentCashEnabled()));
        return repo.save(s);
    }

    public SiteSettings updateStockAlerts(SiteSettingsDtos.StockAlertSettingsRequest req) {
        SiteSettings s = get();
        s.setLowStockAlertEnabled(Boolean.TRUE.equals(req.lowStockAlertEnabled()));
        s.setLowStockAlertEmail(blankToNull(req.lowStockAlertEmail()));
        return repo.save(s);
    }

    private static SiteSettings defaults() {
        SiteSettings s = new SiteSettings();
        s.setTheme("default");
        s.setWhatsappIntro(DEFAULT_WHATSAPP_INTRO);
        s.setWhatsappClosing(DEFAULT_WHATSAPP_CLOSING);
        return s;
    }

    private static String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}
