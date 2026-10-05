package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.KdsDtos.KdsItem;
import com.saasweb.restaurante.dto.KdsDtos.KdsOrden;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Vista de cocina (KDS), separada del flujo de toma de pedido: sólo lee/avanza
 * el estado de {@link OrdenItem}, no toca {@code OrdenService}.
 */
@Service
public class KdsService {

    private final OrdenItemRepository ordenItemRepository;

    public KdsService(OrdenItemRepository ordenItemRepository) {
        this.ordenItemRepository = ordenItemRepository;
    }

    public List<KdsOrden> listarPendientes() {
        List<OrdenItem> items = ordenItemRepository.findParaCocina(tenantId());

        Map<Long, Orden> ordenesPorId = new LinkedHashMap<>();
        Map<Long, List<OrdenItem>> itemsPorOrdenId = new LinkedHashMap<>();
        for (OrdenItem item : items) {
            Long ordenId = item.getOrden().getId();
            ordenesPorId.putIfAbsent(ordenId, item.getOrden());
            itemsPorOrdenId.computeIfAbsent(ordenId, k -> new java.util.ArrayList<>()).add(item);
        }

        return ordenesPorId.values().stream()
                .map(orden -> new KdsOrden(orden.getId(), orden.getTipo(),
                        orden.getMesa() != null ? orden.getMesa().getNombre() : null, orden.getFechaCreacion(),
                        itemsPorOrdenId.get(orden.getId()).stream().map(KdsItem::from).toList()))
                .toList();
    }

    public OrdenItem avanzarEstado(Long itemId) {
        OrdenItem item = ordenItemRepository.findByIdAndOrdenTenantId(itemId, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado"));
        item.setEstado(siguiente(item.getEstado()));
        return ordenItemRepository.save(item);
    }

    private OrdenItem.Estado siguiente(OrdenItem.Estado actual) {
        return switch (actual) {
            case PENDIENTE -> OrdenItem.Estado.EN_PREPARACION;
            case EN_PREPARACION -> OrdenItem.Estado.LISTO;
            case LISTO -> OrdenItem.Estado.ENTREGADO;
            case ENTREGADO -> throw new BadRequestException("El ítem ya fue entregado");
        };
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
