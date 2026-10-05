package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.AutopedidoDtos.PedidoPublico;
import com.saasweb.restaurante.dto.AutopedidoDtos.ProductoAutopedido;
import com.saasweb.restaurante.dto.OrdenDtos.AgregarItemRequest;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Autopedido desde la mesa vía el token de sesión de la {@link Orden} (ver
 * {@code OrdenService#abrirMesa}). Deliberadamente separado del flujo interno
 * de Órdenes: sólo delega en {@link OrdenService#agregarItem} para no duplicar
 * la lógica de precios/subtotales.
 */
@Service
public class AutopedidoService {

    private final OrdenRepository ordenRepository;
    private final OrdenService ordenService;
    private final ProductoRepository productoRepository;
    private final SolicitudMesaRepository solicitudRepository;

    public AutopedidoService(OrdenRepository ordenRepository, OrdenService ordenService,
                              ProductoRepository productoRepository, SolicitudMesaRepository solicitudRepository) {
        this.ordenRepository = ordenRepository;
        this.ordenService = ordenService;
        this.productoRepository = productoRepository;
        this.solicitudRepository = solicitudRepository;
    }

    public PedidoPublico pedido(String token) {
        Orden orden = ordenPorToken(token);
        List<OrdenItem> items = ordenService.items(orden.getId());
        return new PedidoPublico(orden.getId(), orden.getMesa() != null ? orden.getMesa().getNombre() : null,
                orden.getEstado(), orden.getTotal(), items.stream().map(AutopedidoDtos.ItemPedido::from).toList());
    }

    public List<ProductoAutopedido> catalogo(String token) {
        Orden orden = ordenPorToken(token);
        return productoRepository.findByTenantIdOrderByNombreAsc(orden.getTenantId()).stream()
                .filter(Producto::isActivo)
                .filter(Producto::isDisponible)
                .filter(Producto::isDisponibleOnline)
                .filter(p -> p.getTipo() == Producto.Tipo.VENDIBLE || p.getTipo() == Producto.Tipo.COMBO)
                .map(ProductoAutopedido::from)
                .toList();
    }

    public PedidoPublico agregarItem(String token, AgregarItemRequest request) {
        Orden orden = ordenPorToken(token);
        ordenService.agregarItem(orden.getId(), request);
        return pedido(token);
    }

    public void solicitar(String token, SolicitudMesa.Tipo tipo) {
        Orden orden = ordenPorToken(token);
        SolicitudMesa solicitud = new SolicitudMesa();
        solicitud.setTenantId(orden.getTenantId());
        solicitud.setOrden(orden);
        solicitud.setTipo(tipo);
        solicitudRepository.save(solicitud);
    }

    public List<SolicitudMesa> listarPendientes() {
        return solicitudRepository.findPendientes(tenantId());
    }

    public void atender(Long id) {
        SolicitudMesa solicitud = solicitudRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        solicitud.setEstado(SolicitudMesa.Estado.ATENDIDA);
        solicitudRepository.save(solicitud);
    }

    private Orden ordenPorToken(String token) {
        return ordenRepository.findAbiertaPorToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Este pedido ya no está disponible"));
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
