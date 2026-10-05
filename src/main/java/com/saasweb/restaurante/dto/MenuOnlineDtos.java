package com.saasweb.restaurante.dto;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class MenuOnlineDtos {

    private MenuOnlineDtos() {
    }

    public record RedSocialDto(String plataforma, String url, String nombreMostrar) {
    }

    public record ConfigRequest(
            MenuOnlineConfig.FormaLogo formaLogo,
            MenuOnlineConfig.Layout layout,
            int columnasMax,
            MenuOnlineConfig.Tipografia tipografia,
            String encabezadoTexto,
            boolean mostrarDescripciones,
            boolean mostrarDireccion,
            List<RedSocialDto> redesSociales
    ) {
    }

    public record ConfigResponse(
            String slug,
            String logoUrl,
            MenuOnlineConfig.FormaLogo formaLogo,
            MenuOnlineConfig.Layout layout,
            int columnasMax,
            MenuOnlineConfig.Tipografia tipografia,
            String encabezadoTexto,
            boolean mostrarDescripciones,
            boolean mostrarDireccion,
            List<RedSocialDto> redesSociales
    ) {
        public static ConfigResponse from(MenuOnlineConfig c) {
            return new ConfigResponse(c.getSlug(), c.getLogoUrl(), c.getFormaLogo(), c.getLayout(),
                    c.getColumnasMax(), c.getTipografia(), c.getEncabezadoTexto(), c.isMostrarDescripciones(),
                    c.isMostrarDireccion(),
                    c.getRedesSociales().stream()
                            .map(r -> new RedSocialDto(r.getPlataforma(), r.getUrl(), r.getNombreMostrar()))
                            .toList());
        }
    }

    public record ProductoPublico(Long id, String nombre, String descripcion, BigDecimal precio, int likes) {
    }

    public record CategoriaPublica(Long id, String nombre, List<ProductoPublico> productos) {
    }

    public record ZonaEnvioPublica(Long id, String nombre, BigDecimal costo) {
    }

    public record CartaPublica(
            String nombreRestaurante,
            String direccion,
            String telefono,
            String logoUrl,
            MenuOnlineConfig.FormaLogo formaLogo,
            MenuOnlineConfig.Layout layout,
            int columnasMax,
            String encabezadoTexto,
            boolean mostrarDescripciones,
            boolean mostrarDireccion,
            List<RedSocialDto> redesSociales,
            List<CategoriaPublica> categorias,
            List<ZonaEnvioPublica> zonasEnvio
    ) {
    }

    public record ComentarioRequest(@NotBlank String nombreCliente, @NotBlank String texto) {
    }

    public record ComentarioResponse(Long id, String nombreCliente, String texto, Instant fecha) {
        public static ComentarioResponse from(ComentarioProducto c) {
            return new ComentarioResponse(c.getId(), c.getNombreCliente(), c.getTexto(), c.getFecha());
        }
    }

    public record ComentarioAdmin(Long id, Long productoId, String productoNombre, String nombreCliente, String texto, Instant fecha) {
        public static ComentarioAdmin from(ComentarioProducto c) {
            return new ComentarioAdmin(c.getId(), c.getProducto().getId(), c.getProducto().getNombre(),
                    c.getNombreCliente(), c.getTexto(), c.getFecha());
        }
    }

    public record ProductoDetallePublico(
            Long id, String nombre, String descripcion, BigDecimal precio, int likes,
            List<ComentarioResponse> comentarios
    ) {
    }

    public record LikeResponse(int likes) {
    }

    public record AnaliticaProducto(Long productoId, String productoNombre, int vistas, int likes, long comentarios) {
    }
}
