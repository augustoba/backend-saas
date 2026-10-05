package com.saasweb.core.dto;

import com.saasweb.core.model.Dominio;
import com.saasweb.core.model.EstadoDominio;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class DominioDtos {

    private DominioDtos() {
    }

    public record DominioRequest(@NotBlank String host) {
    }

    public record DominioResponse(String id, String host, boolean principal, EstadoDominio estado) {
        public static DominioResponse from(Dominio d) {
            return new DominioResponse(d.getId(), d.getHost(), d.isPrincipal(), d.getEstado());
        }
    }

    /**
     * Lo que el panel necesita para la pantalla de dominios: el subdominio
     * gratis, la URL pública actual, la IP a la que hay que apuntar el DNS
     * (vacía en desarrollo) y los dominios propios.
     */
    public record DominiosResponse(String subdominio, String urlPublica, String ipServidor,
                                   List<DominioResponse> dominios) {
    }
}
