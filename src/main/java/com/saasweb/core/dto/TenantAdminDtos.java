package com.saasweb.core.dto;

import com.saasweb.core.model.Rubro;
import com.saasweb.core.model.Tenant;
import com.saasweb.core.service.TenantDeletionService;

import com.saasweb.core.model.Sistema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

public class TenantAdminDtos {

    /**
     * Alta de tenant (ver PLAN_SAAS.md Fase 10, asistente "Crear tienda").
     * Sólo {@code name}/{@code slug}/{@code rubro} son obligatorios — el
     * resto son los datos que el asistente junta ANTES de crear nada (diseño,
     * color, identidad) y se mandan todos juntos recién en el paso final, así
     * la tienda nace ya configurada en vez de crearse vacía y editarse después.
     */
    public record TenantCreateRequest(
            @NotBlank String name,
            @NotBlank @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
                    message = "El identificador sólo puede tener minúsculas, números y guiones.") String slug,
            @NotNull Rubro rubro,
            @Pattern(regexp = SiteSettingsDtos.LAYOUT_PATTERN, message = "Diseño desconocido")
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
            String pageBackgroundColor,
            @Pattern(regexp = "^$|^\\d{8,15}$", message = "WhatsApp: sólo números (8 a 15 dígitos)")
            String whatsappNumber,
            String instagram,
            String facebookUrl,
            @jakarta.validation.constraints.Size(max = 5_000_000) String logoUrl,
            @Pattern(regexp = "^$|^(circle|square|rectangle)$", message = "Forma de logo desconocida")
            String logoShape,
            /**
             * Plan a asignar: tiene que ser del mismo sistema. Vacío = el plan
             * de partida de ese sistema.
             */
            String planId,
            /**
             * Sistema de la tienda (ecommerce, POS o restaurante). Vacío = el
             * del plan elegido, o ecommerce (compat con el asistente actual).
             */
            Sistema sistema) {
    }

    public record TenantResponse(String id, String slug, String name, Rubro rubro, String rubroLabel,
                                 boolean active, Instant createdAt,
                                 Sistema sistema,
                                 /** `sistema == ECOMMERCE`: tiene tienda online (lo sigue leyendo el front). */
                                 boolean ecommerceSiteEnabled) {
        public static TenantResponse from(Tenant t) {
            return new TenantResponse(t.getId(), t.getSlug(), t.getName(), t.getRubro(),
                    t.getRubro().getLabel(), t.isActive(), t.getCreatedAt(), t.getSistema(),
                    t.getSistema() == Sistema.ECOMMERCE);
        }
    }

    /** Pausar/reanudar una tienda (ver TenantResolutionFilter). */
    public record TenantActiveRequest(@NotNull Boolean active) {
    }

    /** Borrado permanente (ver TenantDeletionService) — confirmSlug tiene que ser el slug exacto de la tienda. */
    public record TenantDeleteRequest(@NotBlank String confirmSlug) {
    }
}
