package com.saasweb.restaurante.controller;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ReporteDtos.ReporteProductos;
import com.saasweb.restaurante.dto.ReporteDtos.ReportePropinas;
import com.saasweb.restaurante.dto.ReporteDtos.ReportePyL;
import com.saasweb.restaurante.dto.ReporteDtos.ReporteVentas;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PreAuthorize("hasAuthority('REPORTE_VENTAS')")
    @GetMapping("/api/restaurante/reportes/ventas")
    public ReporteVentas ventas(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.ventas(desde, hasta);
    }

    @PreAuthorize("hasAuthority('REPORTE_PROPINAS')")
    @GetMapping("/api/restaurante/reportes/propinas")
    public ReportePropinas propinas(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.propinas(desde, hasta);
    }

    @PreAuthorize("hasAuthority('REPORTE_PYL')")
    @GetMapping("/api/restaurante/reportes/pyl")
    public ReportePyL pyl(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.pyL(desde, hasta);
    }

    @PreAuthorize("hasAuthority('REPORTE_VENTAS')")
    @GetMapping("/api/restaurante/reportes/productos")
    public ReporteProductos productos(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.productos(desde, hasta);
    }
}
