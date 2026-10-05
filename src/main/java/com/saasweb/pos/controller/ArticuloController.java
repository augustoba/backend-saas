package com.saasweb.pos.controller;

import com.saasweb.pos.dto.ArticuloDtos.*;
import com.saasweb.pos.service.ArticuloService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Artículos, categorías y stock del punto de venta. */
@RestController
@RequestMapping("/api/pos")
public class ArticuloController {

    private final ArticuloService service;

    public ArticuloController(ArticuloService service) {
        this.service = service;
    }

    @GetMapping("/articulos")
    @PreAuthorize("hasAnyAuthority('PRODUCTS_VIEW','POS_USE')")
    public List<ArticuloResponse> listar(@RequestParam(defaultValue = "false") boolean archivados) {
        return service.listar(archivados).stream().map(ArticuloResponse::from).toList();
    }

    @GetMapping("/articulos/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCTS_VIEW','POS_USE')")
    public ArticuloResponse get(@PathVariable String id) {
        return ArticuloResponse.from(service.get(id));
    }

    /** Lo que usa el lector de códigos al vender. */
    @GetMapping("/articulos/codigo/{codigo}")
    @PreAuthorize("hasAnyAuthority('PRODUCTS_VIEW','POS_USE')")
    public ArticuloResponse porCodigo(@PathVariable String codigo) {
        return ArticuloResponse.from(service.porCodigo(codigo));
    }

    @PostMapping("/articulos")
    @PreAuthorize("hasAuthority('PRODUCTS_MANAGE')")
    public ResponseEntity<ArticuloResponse> crear(@Valid @RequestBody ArticuloRequest req, Authentication auth) {
        return ResponseEntity.status(201).body(ArticuloResponse.from(service.crear(req, auth.getName())));
    }

    @PutMapping("/articulos/{id}")
    @PreAuthorize("hasAuthority('PRODUCTS_MANAGE')")
    public ArticuloResponse actualizar(@PathVariable String id, @Valid @RequestBody ArticuloRequest req) {
        return ArticuloResponse.from(service.actualizar(id, req));
    }

    @PostMapping("/articulos/{id}/archivar")
    @PreAuthorize("hasAuthority('PRODUCTS_MANAGE')")
    public ArticuloResponse archivar(@PathVariable String id) {
        return ArticuloResponse.from(service.archivar(id, true));
    }

    @PostMapping("/articulos/{id}/restaurar")
    @PreAuthorize("hasAuthority('PRODUCTS_MANAGE')")
    public ArticuloResponse restaurar(@PathVariable String id) {
        return ArticuloResponse.from(service.archivar(id, false));
    }

    @PostMapping("/articulos/{id}/stock")
    @PreAuthorize("hasAuthority('PRODUCTS_MANAGE')")
    public ArticuloResponse stock(@PathVariable String id, @Valid @RequestBody StockRequest req, Authentication auth) {
        return ArticuloResponse.from(service.cambiarStock(id, req, auth.getName()));
    }

    @GetMapping("/stock/movimientos")
    @PreAuthorize("hasAnyAuthority('STOCK_MOVEMENTS_VIEW','PRODUCTS_MANAGE')")
    public List<MovimientoResponse> movimientos(@RequestParam(required = false) String articuloId,
                                                @RequestParam(defaultValue = "200") int limite) {
        return service.movimientos(articuloId, limite).stream().map(MovimientoResponse::from).toList();
    }

    @GetMapping("/categorias")
    @PreAuthorize("hasAnyAuthority('PRODUCTS_VIEW','POS_USE')")
    public List<CategoriaResponse> categorias() {
        return service.categorias().stream().map(CategoriaResponse::from).toList();
    }

    @PostMapping("/categorias")
    @PreAuthorize("hasAuthority('PRODUCTS_MANAGE')")
    public ResponseEntity<CategoriaResponse> crearCategoria(@Valid @RequestBody CategoriaRequest req) {
        return ResponseEntity.status(201).body(CategoriaResponse.from(service.crearCategoria(req)));
    }

    @PutMapping("/categorias/{id}")
    @PreAuthorize("hasAuthority('PRODUCTS_MANAGE')")
    public CategoriaResponse actualizarCategoria(@PathVariable String id, @Valid @RequestBody CategoriaRequest req) {
        return CategoriaResponse.from(service.actualizarCategoria(id, req));
    }

    @DeleteMapping("/categorias/{id}")
    @PreAuthorize("hasAuthority('PRODUCTS_MANAGE')")
    public ResponseEntity<Void> borrarCategoria(@PathVariable String id) {
        service.borrarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}
