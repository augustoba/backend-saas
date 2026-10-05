package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ImportarDtos.ImportarResultado;
import com.saasweb.restaurante.dto.ProductoDtos.CategoriaRequest;
import com.saasweb.restaurante.dto.ProductoDtos.CategoriaResponse;
import com.saasweb.restaurante.dto.ProductoDtos.DisponibilidadRequest;
import com.saasweb.restaurante.dto.ProductoDtos.ProductoRequest;
import com.saasweb.restaurante.dto.ProductoDtos.ProductoResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/api/restaurante/categorias")
    public List<CategoriaResponse> listarCategorias() {
        return productoService.listarCategorias().stream().map(CategoriaResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('PRODUCTO_CREAR')")
    @PostMapping("/api/restaurante/categorias")
    public CategoriaResponse crearCategoria(@Valid @RequestBody CategoriaRequest request) {
        return CategoriaResponse.from(productoService.crearCategoria(request));
    }

    @GetMapping("/api/restaurante/productos")
    public List<ProductoResponse> listar() {
        return productoService.listar().stream().map(ProductoResponse::from).toList();
    }

    @PreAuthorize("hasAuthority('PRODUCTO_CREAR')")
    @PostMapping("/api/restaurante/productos")
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
        return ProductoResponse.from(productoService.crear(request));
    }

    @PreAuthorize("hasAuthority('PRODUCTO_EDITAR')")
    @PutMapping("/api/restaurante/productos/{id}")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return ProductoResponse.from(productoService.actualizar(id, request));
    }

    @PreAuthorize("hasAuthority('PRODUCTO_ACTUALIZAR_PRECIO')")
    @PatchMapping("/api/restaurante/productos/{id}/disponibilidad")
    public ProductoResponse cambiarDisponibilidad(@PathVariable Long id, @Valid @RequestBody DisponibilidadRequest request) {
        return ProductoResponse.from(productoService.cambiarDisponibilidad(id, request.disponible()));
    }

    @PreAuthorize("hasAuthority('PRODUCTO_CREAR')")
    @PostMapping("/api/restaurante/productos/importar")
    public ImportarResultado importar(@RequestParam("archivo") MultipartFile archivo) {
        return productoService.importarCsv(archivo);
    }

    @PreAuthorize("hasAuthority('PRODUCTO_EDITAR')")
    @DeleteMapping("/api/restaurante/productos/{id}")
    public void archivar(@PathVariable Long id) {
        productoService.archivar(id);
    }
}
