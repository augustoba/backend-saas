package com.saasweb.core.dto;

import com.saasweb.core.model.ConfigArca;
import com.saasweb.core.model.ConfigImagenes;
import com.saasweb.core.model.ConfigMail;
import com.saasweb.core.model.ConfigMp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTOs de la identidad y las configuraciones del núcleo (ver {@code ConfiguracionService}). */
public final class ConfiguracionDtos {

    private ConfiguracionDtos() {}

    /** Identidad del comercio (la parte de identidad del formulario "Identidad y contacto"). */
    public record IdentidadRequest(
            @NotBlank String storeName,
            @NotBlank
            @Pattern(regexp = "\\d{8,15}", message = "Solo números, sin +, espacios ni 15 (8 a 15 dígitos)")
            String whatsappNumber,
            String aboutText,
            String instagram,
            String facebookUrl,
            @Size(max = 5_000_000) String logoUrl,
            @Pattern(regexp = "^$|^(circle|square|rectangle)$", message = "Forma de logo desconocida")
            String logoShape,
            @Size(max = 500) String storeAddress,
            @Size(max = 5_000_000) String storePhotoUrl
    ) {}

    /**
     * Credenciales de Mercado Pago del tenant (Fase 13) — editable por
     * `PAYMENTS_MANAGE` (es SU cuenta, no una compartida de plataforma).
     * `accessToken` en blanco = no tocar el que ya está guardado (mismo
     * criterio que `MailConfigRequest.password`).
     */
    public record MercadoPagoConfigRequest(
            Boolean mpEnabled,
            @Size(max = 300) String accessToken,
            @Size(max = 300) String publicKey
    ) {}

    /** {@code accessToken} nunca se devuelve: sólo si hay uno guardado (accessTokenSet). */
    public record MercadoPagoConfigResponse(boolean mpEnabled, boolean accessTokenSet, String publicKey) {
        public static MercadoPagoConfigResponse from(ConfigMp s) {
            return new MercadoPagoConfigResponse(
                    s.isMpEnabled(),
                    s.getMpAccessToken() != null && !s.getMpAccessToken().isBlank(),
                    s.getMpPublicKey());
        }
    }

    /**
     * Credenciales de ARCA del tenant (Fase 14) — editable por
     * `PAYMENTS_MANAGE` (es SU CUIT, no uno compartido de plataforma).
     * `certificadoPem`/`clavePrivadaPem` en blanco = no tocar los ya
     * guardados (mismo criterio que `MercadoPagoConfigRequest.accessToken`).
     */
    public record ArcaConfigRequest(
            Boolean arcaEnabled,
            Boolean arcaModoPrueba,
            @Size(max = 20) String cuit,
            Integer puntoVenta,
            @Size(max = 40) String condicionIva,
            @Size(max = 8000) String certificadoPem,
            @Size(max = 8000) String clavePrivadaPem,
            @Pattern(regexp = "^(TICKET_INTERNO|FACTURA_ARCA)$", message = "Modo de comprobante desconocido")
            String invoiceMode
    ) {}

    /** El certificado/clave nunca se devuelven: sólo si hay uno guardado (ver `*Set`). */
    public record ArcaConfigResponse(
            boolean arcaEnabled, boolean arcaModoPrueba, String cuit, Integer puntoVenta,
            String condicionIva, boolean certificadoSet, boolean clavePrivadaSet, String invoiceMode
    ) {
        public static ArcaConfigResponse from(ConfigArca s) {
            return new ArcaConfigResponse(
                    s.isArcaEnabled(), s.isArcaModoPrueba(), s.getArcaCuit(), s.getArcaPuntoVenta(),
                    s.getArcaCondicionIva(),
                    s.getArcaCertificadoPem() != null && !s.getArcaCertificadoPem().isBlank(),
                    s.getArcaClavePrivadaPem() != null && !s.getArcaClavePrivadaPem().isBlank(),
                    s.getInvoiceMode());
        }
    }

    /** Config de Cloudinary: sólo la puede ver/editar un superadmin. */
    /** {@code apiKey}/{@code apiSecret}: opcionales, sólo para poder borrar fotos. Secret en blanco = no cambiarlo. */
    public record CloudinaryConfigRequest(
            @Size(max = 200) String cloudName,
            @Size(max = 200) String uploadPreset,
            @Size(max = 100) String apiKey,
            @Size(max = 200) String apiSecret
    ) {}

    /** El secret nunca se devuelve: sólo si hay uno guardado. */
    public record CloudinaryConfigResponse(String cloudName, String uploadPreset, String apiKey, boolean apiSecretSet) {
        public static CloudinaryConfigResponse from(ConfigImagenes s) {
            return new CloudinaryConfigResponse(s.getCloudinaryCloudName(), s.getCloudinaryUploadPreset(),
                    s.getCloudinaryApiKey(), s.getCloudinaryApiSecret() != null && !s.getCloudinaryApiSecret().isBlank());
        }
    }

    /**
     * Config de SMTP (recuperación de cuenta por mail): sólo la puede ver/editar
     * un superadmin. {@code password} vacío/null = no cambiar la que ya está
     * guardada (mismo patrón que el cambio de contraseña de un AdminUser).
     */
    public record MailConfigRequest(
            @Size(max = 300) String host,
            Integer port,
            @Size(max = 300) String username,
            @Size(max = 500) String password,
            @Size(max = 300) String fromEmail,
            @Size(max = 200) String fromName
    ) {}

    /** {@code password} nunca se devuelve: sólo si hay una guardada (passwordSet). */
    public record MailConfigResponse(
            String host, Integer port, String username,
            boolean passwordSet, String fromEmail, String fromName
    ) {
        public static MailConfigResponse from(ConfigMail s) {
            return new MailConfigResponse(
                    s.getSmtpHost(), s.getSmtpPort(), s.getSmtpUsername(),
                    s.getSmtpPassword() != null && !s.getSmtpPassword().isBlank(),
                    s.getSmtpFromEmail(), s.getSmtpFromName());
        }
    }
}
