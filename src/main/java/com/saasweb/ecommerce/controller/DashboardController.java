package com.saasweb.ecommerce.controller;

import com.saasweb.ecommerce.dto.DashboardDtos.DashboardResponse;
import com.saasweb.ecommerce.dto.DashboardDtos.ExpiringCaeItem;
import com.saasweb.ecommerce.dto.DashboardDtos.LowStockItem;
import com.saasweb.ecommerce.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Resumen del panel de administración. Sólo admin (`/api/admin/**`). */
@RestController
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/api/admin/dashboard")
    public DashboardResponse dashboard() {
        return service.get();
    }

    /** Sólo la lista de talles por reponer (para el badge del menú y la pantalla de stock). */
    @GetMapping("/api/admin/low-stock")
    public List<LowStockItem> lowStock() {
        return service.lowStock();
    }

    /** Facturas ARCA cuyo CAE vence pronto (para el badge del menú, ítem 5). */
    @GetMapping("/api/admin/expiring-cae")
    public List<ExpiringCaeItem> expiringCae(@RequestParam(defaultValue = "15") int days) {
        return service.expiringCae(Math.min(Math.max(days, 1), 90));
    }
}
