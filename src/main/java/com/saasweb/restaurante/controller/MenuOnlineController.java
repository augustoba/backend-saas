package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.MenuOnlineDtos.AnaliticaProducto;
import com.saasweb.restaurante.dto.MenuOnlineDtos.CartaPublica;
import com.saasweb.restaurante.dto.MenuOnlineDtos.ComentarioAdmin;
import com.saasweb.restaurante.dto.MenuOnlineDtos.ComentarioRequest;
import com.saasweb.restaurante.dto.MenuOnlineDtos.ComentarioResponse;
import com.saasweb.restaurante.dto.MenuOnlineDtos.ConfigRequest;
import com.saasweb.restaurante.dto.MenuOnlineDtos.ConfigResponse;
import com.saasweb.restaurante.dto.MenuOnlineDtos.LikeResponse;
import com.saasweb.restaurante.dto.MenuOnlineDtos.ProductoDetallePublico;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MenuOnlineController {

    private final MenuOnlineService menuOnlineService;

    public MenuOnlineController(MenuOnlineService menuOnlineService) {
        this.menuOnlineService = menuOnlineService;
    }

    @GetMapping("/api/restaurante/menu-online/config")
    public ConfigResponse obtenerConfig() {
        return ConfigResponse.from(menuOnlineService.obtenerOCrear());
    }

    @PreAuthorize("hasAuthority('MENU_ONLINE_CONFIGURAR')")
    @PutMapping("/api/restaurante/menu-online/config")
    public ConfigResponse actualizarConfig(@Valid @RequestBody ConfigRequest request) {
        return ConfigResponse.from(menuOnlineService.actualizar(request));
    }

    @PreAuthorize("hasAuthority('MENU_ONLINE_CONFIGURAR')")
    @GetMapping("/api/restaurante/menu-online/analiticas")
    public List<AnaliticaProducto> analiticas() {
        return menuOnlineService.analiticas();
    }

    @PreAuthorize("hasAuthority('MENU_ONLINE_CONFIGURAR')")
    @GetMapping("/api/restaurante/menu-online/comentarios")
    public List<ComentarioAdmin> listarComentarios() {
        return menuOnlineService.listarComentarios().stream().map(ComentarioAdmin::from).toList();
    }

    @PreAuthorize("hasAuthority('MENU_ONLINE_CONFIGURAR')")
    @DeleteMapping("/api/restaurante/menu-online/comentarios/{id}")
    public void eliminarComentario(@PathVariable Long id) {
        menuOnlineService.eliminarComentario(id);
    }

    @GetMapping("/api/public/restaurante/carta/{slug}")
    public CartaPublica cartaPublica(@PathVariable String slug) {
        return menuOnlineService.cartaPublica(slug);
    }

    @GetMapping("/api/public/restaurante/carta/{slug}/productos/{productoId}")
    public ProductoDetallePublico detalleProducto(@PathVariable String slug, @PathVariable Long productoId) {
        return menuOnlineService.detalleProducto(slug, productoId);
    }

    @PostMapping("/api/public/restaurante/carta/{slug}/productos/{productoId}/like")
    public LikeResponse darLike(@PathVariable String slug, @PathVariable Long productoId) {
        return new LikeResponse(menuOnlineService.darLike(slug, productoId));
    }

    @PostMapping("/api/public/restaurante/carta/{slug}/productos/{productoId}/comentarios")
    public ComentarioResponse comentar(@PathVariable String slug, @PathVariable Long productoId,
                                        @Valid @RequestBody ComentarioRequest request) {
        return ComentarioResponse.from(menuOnlineService.comentar(slug, productoId, request));
    }
}
