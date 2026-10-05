package com.saasweb.core.controller;

import com.saasweb.core.dto.ImagenDtos.SubidaRequest;
import com.saasweb.core.service.ImagenService;
import com.saasweb.core.service.ProveedorImagenes.TicketSubida;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Ticket para que el panel suba una foto directo al proveedor de imágenes de la tienda. */
@RestController
public class ImagenController {

    private final ImagenService service;

    public ImagenController(ImagenService service) {
        this.service = service;
    }

    @PostMapping("/api/core/imagenes/subidas")
    @PreAuthorize("hasRole('ADMIN')")
    public TicketSubida ticket(@Valid @RequestBody SubidaRequest req, Authentication auth) {
        String destino = req.slugTienda() == null || req.slugTienda().isBlank() ? null : req.slugTienda();
        if (destino != null && auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("SUPERADMIN"))) {
            throw new AccessDeniedException("Sólo el superadmin puede subir imágenes a otra tienda.");
        }
        return service.ticket(req.carpeta(), req.nombre(), destino);
    }
}
