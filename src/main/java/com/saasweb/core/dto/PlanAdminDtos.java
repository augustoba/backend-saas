package com.saasweb.core.dto;

import com.saasweb.core.model.Modules;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.model.Plan;
import com.saasweb.core.controller.PlanController;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/** DTOs de administración de planes (ver PlanController) — sólo superadmin. */
public final class PlanAdminDtos {

    private PlanAdminDtos() {
    }

    public record PlanResponse(
            String id, String slug, String name,
            /** Sistema del plan (ver {@code Sistema}). Fijo, no se edita. */
            Sistema sistema,
            /** Igual a {@code sistema}: lo sigue leyendo el editor de planes del front hasta que pase a `sistema`. */
            String businessModel,
            Integer maxProducts, Integer maxAdminUsers,
            Set<String> enabledModules,
            /** Los únicos módulos que el editor debería dejar tildar para este plan (ver {@code Modules.compatibleWith}). */
            Set<String> compatibleModules,
            boolean showPlatformBranding,
            /** Cuántas tiendas usan este plan hoy — contexto antes de cambiarle límites/módulos. */
            long tenantCount
    ) {
        public static PlanResponse from(Plan p, long tenantCount) {
            return new PlanResponse(p.getId(), p.getSlug(), p.getName(), p.getSistema(), p.getSistema().name(),
                    p.getMaxProducts(), p.getMaxAdminUsers(),
                    p.getEnabledModules(), Modules.compatibleWith(p.getSistema()),
                    p.isShowPlatformBranding(), tenantCount);
        }
    }

    /**
     * {@code maxProducts}/{@code maxAdminUsers} en {@code null} = sin límite.
     * El slug no se edita (es el identificador estable del plan).
     */
    public record PlanUpdateRequest(
            @NotBlank String name,
            Integer maxProducts,
            Integer maxAdminUsers,
            @NotNull Set<String> enabledModules,
            boolean showPlatformBranding
    ) {
    }
}
