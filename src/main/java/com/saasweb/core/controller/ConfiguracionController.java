package com.saasweb.core.controller;

import com.saasweb.core.dto.ConfiguracionDtos.*;
import com.saasweb.core.service.ConfiguracionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Credenciales del núcleo de la tienda: Mercado Pago y ARCA (las edita el
 * dueño con PAYMENTS_MANAGE: son SU cuenta y SU CUIT), Cloudinary y SMTP
 * (sólo superadmin). Mantienen las URLs de cuando vivían en el controller de
 * settings, así el front no cambia.
 */
@RestController
public class ConfiguracionController {

    private final ConfiguracionService configuracion;

    public ConfiguracionController(ConfiguracionService configuracion) {
        this.configuracion = configuracion;
    }

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

    @GetMapping("/api/admin/settings/cloudinary")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public CloudinaryConfigResponse cloudinaryConfig() {
        return CloudinaryConfigResponse.from(configuracion.imagenes());
    }

    @PutMapping("/api/admin/settings/cloudinary")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public CloudinaryConfigResponse updateCloudinaryConfig(@Valid @RequestBody CloudinaryConfigRequest req) {
        return CloudinaryConfigResponse.from(configuracion.actualizarImagenes(req.cloudName(), req.uploadPreset(),
                req.apiKey(), req.apiSecret()));
    }

    @GetMapping("/api/admin/settings/mail")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public MailConfigResponse mailConfig() {
        return MailConfigResponse.from(configuracion.mail());
    }

    @PutMapping("/api/admin/settings/mail")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public MailConfigResponse updateMailConfig(@Valid @RequestBody MailConfigRequest req) {
        return MailConfigResponse.from(configuracion.actualizarMail(req.host(), req.port(), req.username(),
                req.password(), req.fromEmail(), req.fromName()));
    }
}
