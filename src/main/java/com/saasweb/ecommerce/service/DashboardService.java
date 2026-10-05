package com.saasweb.ecommerce.service;

import com.saasweb.ecommerce.dto.DashboardDtos;

import com.saasweb.core.common.TenantContext;
import com.saasweb.ecommerce.dto.DashboardDtos.DashboardResponse;
import com.saasweb.ecommerce.dto.DashboardDtos.LowStockItem;
import com.saasweb.ecommerce.dto.DashboardDtos.MonthSummary;
import com.saasweb.ecommerce.dto.DashboardDtos.ProductCounts;
import com.saasweb.ecommerce.dto.DashboardDtos.RecentOrder;
import com.saasweb.ecommerce.dto.MetricsDtos.Totals;
import com.saasweb.ecommerce.model.Order;
import com.saasweb.ecommerce.model.OrderStatus;
import com.saasweb.ecommerce.model.Product;
import com.saasweb.ecommerce.model.Variante;
import com.saasweb.ecommerce.repository.OrderRepository;
import com.saasweb.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    /** Umbral de stock bajo por defecto (unidades por talle) si el producto no define el suyo. */
    public static final int DEFAULT_LOW_STOCK = 3;
    private static final int RECENT_ORDERS = 6;

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final MetricsService metricsService;

    public DashboardService(OrderRepository orderRepo, ProductRepository productRepo, MetricsService metricsService) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.metricsService = metricsService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse get() {
        String tenantId = TenantContext.getTenantId();
        long pending = orderRepo.countByTenantIdAndStatus(tenantId, OrderStatus.PENDIENTE);

        LocalDate today = LocalDate.now();
        Totals month = metricsService.rangeTotals(today.withDayOfMonth(1), today);

        long active = productRepo.countByTenantIdAndActiveTrueAndDeletedFalse(tenantId);
        long total = productRepo.countByTenantIdAndDeletedFalse(tenantId);

        List<RecentOrder> recent = orderRepo.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .limit(RECENT_ORDERS)
                .map(o -> new RecentOrder(o.getId(), o.getCode(), o.getCustomerName(),
                        o.getTotal(), o.getStatus().name(), o.getCreatedAt()))
                .toList();

        List<LowStockItem> lowStock = lowStock();

        return new DashboardResponse(
                pending,
                new MonthSummary(month.revenue(), month.units(), month.orders()),
                new ProductCounts(active, total - active),
                recent,
                DEFAULT_LOW_STOCK,
                lowStock);
    }

    private static final DateTimeFormatter CAE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Pedidos con Factura ARCA aprobada cuyo CAE vence dentro de {@code days}
     * días (informativo — ver ítem 5 de la ronda de mejoras). `caeVencimiento`
     * viene de ARCA en formato `yyyyMMdd`.
     */
    @Transactional(readOnly = true)
    public List<DashboardDtos.ExpiringCaeItem> expiringCae(int days) {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(days);
        List<DashboardDtos.ExpiringCaeItem> out = new ArrayList<>();
        for (Order o : orderRepo.findByTenantIdAndFacturaCaeIsNotNull(TenantContext.getTenantId())) {
            if (o.getInvoiceCaeVencimiento() == null) continue;
            LocalDate vto;
            try {
                vto = LocalDate.parse(o.getInvoiceCaeVencimiento(), CAE_FMT);
            } catch (DateTimeParseException e) {
                continue;
            }
            if (!vto.isBefore(today) && !vto.isAfter(limit)) {
                out.add(new DashboardDtos.ExpiringCaeItem(o.getId(), o.getCode(), o.getInvoiceType(),
                        o.getInvoiceCaeVencimiento(), java.time.temporal.ChronoUnit.DAYS.between(today, vto)));
            }
        }
        out.sort(Comparator.comparingLong(DashboardDtos.ExpiringCaeItem::diasRestantes));
        return out;
    }

    /** Talles de productos activos cuyo stock está en o por debajo de su umbral. */
    @Transactional(readOnly = true)
    public List<LowStockItem> lowStock() {
        List<LowStockItem> items = new ArrayList<>();
        for (Product p : productRepo.findByTenantIdAndActiveTrueAndDeletedFalseOrderByCreatedAtDesc(TenantContext.getTenantId())) {
            if (p.isDiscontinued()) continue; // el dueño/a marcó "no reponer"
            int threshold = p.getLowStockThreshold() != null ? p.getLowStockThreshold() : DEFAULT_LOW_STOCK;
            for (Variante s : p.getSizeStocks()) {
                if (s.getStock() <= threshold) {
                    items.add(new LowStockItem(p.getId(), p.getName(), s.getSize(), s.getStock(), threshold));
                }
            }
        }
        items.sort(Comparator.comparingInt(LowStockItem::stock)
                .thenComparing(LowStockItem::productName));
        return items;
    }
}
