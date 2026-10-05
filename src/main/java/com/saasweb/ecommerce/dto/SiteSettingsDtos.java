package com.saasweb.ecommerce.dto;

import com.saasweb.core.dto.TenantAdminDtos;

import com.saasweb.core.model.ConfigArca;
import com.saasweb.core.model.ConfigImagenes;
import com.saasweb.core.model.Identidad;
import com.saasweb.ecommerce.model.SiteSettings;
import com.saasweb.core.model.Sistema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class SiteSettingsDtos {

    private SiteSettingsDtos() {}


    /** Configuración de plataforma (identidad, logo, WhatsApp, redes, textos). Sólo superadmin. */
    public record PlatformSettingsRequest(
            @NotBlank String storeName,
            @NotBlank
            @Pattern(regexp = "\\d{8,15}", message = "Solo números, sin +, espacios ni 15 (8 a 15 dígitos)")
            String whatsappNumber,
            String aboutText,
            String instagram,
            String facebookUrl,
            /** Logo: URL o data URI. Vacío = usar el logo por defecto. */
            @Size(max = 5_000_000) String logoUrl,
            @Pattern(regexp = "^$|^(circle|square|rectangle)$", message = "Forma de logo desconocida")
            String logoShape,
            @Size(max = 2000) String whatsappIntro,
            @Size(max = 2000) String whatsappClosing,
            @Size(max = 500) String storeAddress,
            @Size(max = 8000) String helpText,
            @Size(max = 20000) String faqText,
            /** Foto del local (identidad). */
            @Size(max = 5_000_000) String storePhotoUrl,
            Boolean aboutPageEnabled,
            Boolean promoBannerEnabled,
            @Size(max = 5_000_000) String promoBannerImage,
            @Size(max = 500) String promoBannerLink
    ) {}

    /**
     * Diseño de página + color de marca (ver PLAN_SAAS.md Fase 10) — mismo
     * nivel de permiso que identidad/logo. Ambos campos son opcionales:
     * vacío/null = volver al valor por defecto del rubro (`layout`) o a la
     * paleta con nombre de siempre (`brandColor`).
     */
    public record AppearanceRequest(
            @Pattern(regexp = TenantAdminDtos.LAYOUT_PATTERN, message = "Diseño desconocido")
            String layout,
            @Pattern(regexp = "^$|^#[0-9a-fA-F]{6}$", message = "Color inválido (formato #rrggbb)")
            String brandColor,
            @Pattern(regexp = "^$|^#[0-9a-fA-F]{6}$", message = "Color inválido (formato #rrggbb)")
            String headerColor,
            @Pattern(regexp = "^$|^#[0-9a-fA-F]{6}$", message = "Color inválido (formato #rrggbb)")
            String footerColor,
            @Pattern(regexp = "^$|^#[0-9a-fA-F]{6}$", message = "Color inválido (formato #rrggbb)")
            String textColor,
            @Pattern(regexp = "^$|^#[0-9a-fA-F]{6}$", message = "Color inválido (formato #rrggbb)")
            String pageBackgroundColor
    ) {}

    /** Medios de pago. Editable por el admin normal de la tienda. */
    public record PaymentsSettingsRequest(
            Boolean paymentTransferEnabled,
            @Size(max = 200) String paymentTransferAlias,
            Boolean paymentQrTransferEnabled,
            @Size(max = 5_000_000) String paymentQrTransferImage,
            Boolean paymentQrCardEnabled,
            @Size(max = 5_000_000) String paymentQrCardImage,
            @Size(max = 1000) String paymentCardLink,
            Boolean paymentCashEnabled
    ) {}

    public record SettingsResponse(
            String storeName,
            String whatsappNumber,
            String aboutText,
            String instagram,
            String facebookUrl,
            String logoUrl,
            String logoShape,
            String theme,
            String layout,
            String brandColor,
            String headerColor,
            String footerColor,
            String textColor,
            String pageBackgroundColor,
            String whatsappIntro,
            String whatsappClosing,
            String storeAddress,
            String helpText,
            String faqText,
            boolean paymentTransferEnabled,
            String paymentTransferAlias,
            boolean paymentQrTransferEnabled,
            String paymentQrTransferImage,
            boolean paymentQrCardEnabled,
            String paymentQrCardImage,
            String paymentCardLink,
            boolean paymentCashEnabled,
            /**
             * Cuenta de Cloudinary para subir imágenes desde el panel. Sólo lectura acá
             * (se necesitan en cualquier sesión de admin para poder subir fotos); se
             * editan aparte, en `/api/admin/settings/cloudinary` (sólo superadmin).
             */
            String cloudinaryCloudName,
            String cloudinaryUploadPreset,
            /**
             * Módulos habilitados por el plan del tenant (ver
             * {@code com.saasweb.core.model.Modules}). `false` = el panel/
             * checkout no debe mostrar esa opción, sin importar qué otro
             * dato haya cargado.
             */
            boolean socialShareEnabled,
            /**
             * true sólo si el módulo `MERCADOPAGO` está habilitado en el
             * plan Y el tenant activó el checkout Y ya cargó su Access
             * Token — recién ahí tiene sentido ofrecer "Pagar con Mercado
             * Pago" en el carrito. El Access Token en sí NUNCA viaja acá
             * (ver `MercadoPagoConfigResponse`, mismo criterio que SMTP).
             */
            boolean mercadoPagoAvailable,
            /** Sistema de la tienda: decide qué panel y qué sitio público tiene. */
            Sistema sistema,
            /** `sistema == POS` (lo sigue leyendo el menú del panel hasta pasar a `sistema`). */
            boolean posEnabled,
            /** `sistema == ECOMMERCE`: tiene tienda online (lo sigue leyendo el panel). */
            boolean ecommerceSiteEnabled,
            /**
             * true sólo si el módulo `ARCA_INVOICING` está habilitado en el
             * plan Y el tenant cargó CUIT + certificado + punto de venta
             * (ver `ArcaInvoiceService#isAvailable`). El certificado/clave
             * NUNCA viajan acá (ver `ArcaConfigResponse`).
             */
            boolean arcaAvailable,
            /** "TICKET_INTERNO" | "FACTURA_ARCA" — qué ofrece por defecto el punto de venta. */
            String invoiceMode,
            /**
             * "MONOTRIBUTO" | "EXENTO" | "RESPONSABLE_INSCRIPTO" — el panel la
             * usa sólo para decidir si mostrar el campo de CUIT del comprador
             * en el punto de venta (habilita Factura A en vez de B).
             */
            String arcaCondicionIva,
            String storePhotoUrl,
            boolean aboutPageEnabled,
            boolean promoBannerEnabled,
            String promoBannerImage,
            String promoBannerLink
    ) {
        /**
         * Junta la configuración de la tienda online ({@code s}) con la identidad y
         * las configuraciones del núcleo: la respuesta queda igual que cuando todo
         * vivía en `site_settings`.
         */
        public static SettingsResponse from(SiteSettings s, Identidad i, ConfigImagenes img, ConfigArca arca,
                                             boolean socialShareEnabled, boolean mercadoPagoAvailable,
                                             Sistema sistema, boolean arcaAvailable) {
            return new SettingsResponse(
                    i.getStoreName(), i.getWhatsappNumber(), i.getAboutText(),
                    i.getInstagram(), i.getFacebookUrl(), i.getLogoUrl(),
                    i.getLogoShape() != null ? i.getLogoShape() : "circle",
                    s.getTheme() != null ? s.getTheme() : "default",
                    s.getLayout() != null ? s.getLayout() : "classic",
                    i.getBrandColor(),
                    s.getHeaderColor(), s.getFooterColor(), s.getTextColor(), s.getPageBackgroundColor(),
                    s.getWhatsappIntro(), s.getWhatsappClosing(), i.getStoreAddress(),
                    s.getHelpText(), s.getFaqText(),
                    s.isPaymentTransferEnabled(), s.getPaymentTransferAlias(),
                    s.isPaymentQrTransferEnabled(), s.getPaymentQrTransferImage(),
                    s.isPaymentQrCardEnabled(), s.getPaymentQrCardImage(),
                    s.getPaymentCardLink(), s.isPaymentCashEnabled(),
                    img.getCloudinaryCloudName(), img.getCloudinaryUploadPreset(),
                    socialShareEnabled, mercadoPagoAvailable,
                    sistema, sistema == Sistema.POS, sistema == Sistema.ECOMMERCE,
                    arcaAvailable, arca.getInvoiceMode(), arca.getArcaCondicionIva(),
                    i.getStorePhotoUrl(), s.isAboutPageEnabled(), s.isPromoBannerEnabled(),
                    s.getPromoBannerImage(), s.getPromoBannerLink());
        }
    }

    /** Alerta de stock bajo por mail (ver `LowStockAlertScheduler`) — la edita el admin normal de la tienda. */
    public record StockAlertSettingsRequest(Boolean lowStockAlertEnabled, @Email String lowStockAlertEmail) {}

    public record StockAlertSettingsResponse(boolean lowStockAlertEnabled, String lowStockAlertEmail) {
        public static StockAlertSettingsResponse from(SiteSettings s) {
            return new StockAlertSettingsResponse(s.isLowStockAlertEnabled(), s.getLowStockAlertEmail());
        }
    }

}
