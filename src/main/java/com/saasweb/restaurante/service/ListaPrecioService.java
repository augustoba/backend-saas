package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ListaPrecioDtos.ItemRequest;
import com.saasweb.restaurante.dto.ListaPrecioDtos.ListaPrecioRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListaPrecioService {

    private final ListaPrecioRepository listaPrecioRepository;
    private final ListaPrecioItemRepository itemRepository;
    private final ProductoRepository productoRepository;

    public ListaPrecioService(ListaPrecioRepository listaPrecioRepository, ListaPrecioItemRepository itemRepository,
                               ProductoRepository productoRepository) {
        this.listaPrecioRepository = listaPrecioRepository;
        this.itemRepository = itemRepository;
        this.productoRepository = productoRepository;
    }

    public List<ListaPrecio> listar() {
        return listaPrecioRepository.findByTenantIdOrderByNombreAsc(tenantId());
    }

    public ListaPrecio crear(ListaPrecioRequest request) {
        ListaPrecio lista = new ListaPrecio();
        lista.setTenantId(tenantId());
        lista.setNombre(request.nombre());
        lista.setDescripcion(request.descripcion());
        return listaPrecioRepository.save(lista);
    }

    public ListaPrecio actualizar(Long id, ListaPrecioRequest request) {
        ListaPrecio lista = obtener(id);
        lista.setNombre(request.nombre());
        lista.setDescripcion(request.descripcion());
        return listaPrecioRepository.save(lista);
    }

    public void archivar(Long id) {
        ListaPrecio lista = obtener(id);
        lista.setActivo(false);
        listaPrecioRepository.save(lista);
    }

    public List<ListaPrecioItem> listarItems(Long listaId) {
        obtener(listaId);
        return itemRepository.findByListaPrecioId(listaId);
    }

    public ListaPrecioItem upsertItem(Long listaId, ItemRequest request) {
        ListaPrecio lista = obtener(listaId);
        Producto producto = productoRepository.findByIdAndTenantId(request.productoId(), tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        ListaPrecioItem item = itemRepository.findByListaPrecioIdAndProductoId(listaId, request.productoId())
                .orElseGet(() -> {
                    ListaPrecioItem nuevo = new ListaPrecioItem();
                    nuevo.setListaPrecio(lista);
                    nuevo.setProducto(producto);
                    return nuevo;
                });
        item.setPrecio(request.precio());
        return itemRepository.save(item);
    }

    public void eliminarItem(Long listaId, Long itemId) {
        obtener(listaId);
        ListaPrecioItem item = itemRepository.findByListaPrecioId(listaId).stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));
        itemRepository.delete(item);
    }

    private ListaPrecio obtener(Long id) {
        return listaPrecioRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Lista de precios no encontrada"));
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
