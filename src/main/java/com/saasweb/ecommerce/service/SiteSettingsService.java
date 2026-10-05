package com.saasweb.ecommerce.service;

import com.saasweb.core.dto.ConfiguracionDtos;
import com.saasweb.core.service.ConfiguracionService;

import com.saasweb.ecommerce.dto.SiteSettingsDtos;

import com.saasweb.core.common.TenantContext;
import com.saasweb.ecommerce.dto.SiteSettingsDtos.PaymentsSettingsRequest;
import com.saasweb.ecommerce.dto.SiteSettingsDtos.PlatformSettingsRequest;
import com.saasweb.ecommerce.model.SiteSettings;
import com.saasweb.ecommerce.repository.SiteSettingsRepository;
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
     * Crea la configuración de tienda online de un tenant nuevo con su diseño. La
     * llama {@code InicializadorEcommerce} (la identidad la crea el núcleo antes).
     */
    public SiteSettings createFor(String tenantId, String theme, String layout) {
        SiteSettings s = new SiteSettings();
        s.setId(tenantId);
        s.setTheme(theme);
        s.setLayout(layout);
        return repo.save(s);
    }

    /**
     * Colores de la tienda que eligió el asistente "Crear tienda" (null = el
     * default). Directo por id: durante el alta la request todavía está en el
     * contexto del superadmin, no en el de la tienda recién creada.
     */
    public void applyOnboardingColors(String tenantId, String headerColor, String footerColor, String textColor,
                                      String pageBackgroundColor) {
        if (headerColor == null && footerColor == null && textColor == null && pageBackgroundColor == null) return;
        SiteSettings s = repo.findById(tenantId).orElseThrow();
        if (headerColor != null) s.setHeaderColor(headerColor);
        if (footerColor != null) s.setFooterColor(footerColor);
        if (textColor != null) s.setTextColor(textColor);
        if (pageBackgroundColor != null) s.setPageBackgroundColor(pageBackgroundColor);
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
        configuracion.actualizarIdentidad(new ConfiguracionDtos.IdentidadRequest(req.storeName(), req.whatsappNumber(),
                req.aboutText(), req.instagram(), req.facebookUrl(), req.logoUrl(), req.logoShape(), req.storeAddress(),
                req.storePhotoUrl()));
        SiteSettings s = get();
        s.setWhatsappIntro(blankToNull(req.whatsappIntro()));
        s.setWhatsappClosing(blankToNull(req.whatsappClosing()));
        s.setHelpText(blankToNull(req.helpText()));
        s.setFaqText(blankToNull(req.faqText()));
        s.setAboutPageEnabled(Boolean.TRUE.equals(req.aboutPageEnabled()));
        s.setPromoBannerEnabled(Boolean.TRUE.equals(req.promoBannerEnabled()));
        s.setPromoBannerImage(blankToNull(req.promoBannerImage()));
        s.setPromoBannerLink(blankToNull(req.promoBannerLink()));
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
