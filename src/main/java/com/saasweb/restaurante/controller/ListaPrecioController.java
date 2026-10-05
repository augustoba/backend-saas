package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ListaPrecioDtos.ItemRequest;
import com.saasweb.restaurante.dto.ListaPrecioDtos.ItemResponse;
import com.saasweb.restaurante.dto.ListaPrecioDtos.ListaPrecioRequest;
import com.saasweb.restaurante.dto.ListaPrecioDtos.ListaPrecioResponse;
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
public class ListaPrecioController {

    private final ListaPrecioService listaPrecioService;

    public ListaPrecioController(ListaPrecioService listaPrecioService) {
        this.listaPrecioService = listaPrecioService;
    }

    @GetMapping("/api/restaurante/listas-precio")
    public List<ListaPrecioResponse> listar() {
        return listaPrecioService.listar().stream().map(ListaPrecioResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('PRODUCTO_LISTAS_PRECIO_GESTIONAR')")
    @PostMapping("/api/restaurante/listas-precio")
    public ListaPrecioResponse crear(@Valid @RequestBody ListaPrecioRequest request) {
        return ListaPrecioResponse.from(listaPrecioService.crear(request));
    }

    @PreAuthorize("hasAuthority('PRODUCTO_LISTAS_PRECIO_GESTIONAR')")
    @PutMapping("/api/restaurante/listas-precio/{id}")
    public ListaPrecioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ListaPrecioRequest request) {
        return ListaPrecioResponse.from(listaPrecioService.actualizar(id, request));
    }

    @PreAuthorize("hasAuthority('PRODUCTO_LISTAS_PRECIO_GESTIONAR')")
    @DeleteMapping("/api/restaurante/listas-precio/{id}")
    public void archivar(@PathVariable Long id) {
        listaPrecioService.archivar(id);
    }

    @GetMapping("/api/restaurante/listas-precio/{id}/items")
    public List<ItemResponse> listarItems(@PathVariable Long id) {
        return listaPrecioService.listarItems(id).stream().map(ItemResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('PRODUCTO_LISTAS_PRECIO_GESTIONAR')")
    @PostMapping("/api/restaurante/listas-precio/{id}/items")
    public ItemResponse upsertItem(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return ItemResponse.from(listaPrecioService.upsertItem(id, request));
    }

    @PreAuthorize("hasAuthority('PRODUCTO_LISTAS_PRECIO_GESTIONAR')")
    @DeleteMapping("/api/restaurante/listas-precio/{id}/items/{itemId}")
    public void eliminarItem(@PathVariable Long id, @PathVariable Long itemId) {
        listaPrecioService.eliminarItem(id, itemId);
    }
}
