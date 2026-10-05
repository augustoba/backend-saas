package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.KdsDtos.KdsItem;
import com.saasweb.restaurante.dto.KdsDtos.KdsOrden;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KdsController {

    private final KdsService kdsService;

    public KdsController(KdsService kdsService) {
        this.kdsService = kdsService;
    }

    @GetMapping("/api/restaurante/kds/pendientes")
    public List<KdsOrden> listarPendientes() {
        return kdsService.listarPendientes();
    }

    @PreAuthorize("hasAuthority('KDS_OPERAR')")
    @PatchMapping("/api/restaurante/kds/items/{itemId}/avanzar")
    public KdsItem avanzarEstado(@PathVariable Long itemId) {
        return KdsItem.from(kdsService.avanzarEstado(itemId));
    }
}
