package com.saasweb.ecommerce.service;

import com.saasweb.core.service.LimpiezaDeTienda;
import com.saasweb.ecommerce.repository.*;
import org.springframework.stereotype.Component;

/**
 * Borra todo lo que el ecommerce guardó para una tienda (baja definitiva, ver
 * {@code TenantDeletionService}). Pedidos, cambios y parametrías se borran
 * entidad por entidad (no SQL masivo) para que Hibernate respete el
 * {@code cascade=ALL, orphanRemoval=true} hacia sus líneas y opciones.
 */
@Component
public class LimpiezaEcommerce implements LimpiezaDeTienda {

    private final OrderRepository orderRepo;
    private final ExchangeRepository exchangeRepo;
    private final ParamRepository paramRepo;
    private final ProductRepository productRepo;
    private final CouponRepository couponRepo;
    private final DiscountRepository discountRepo;
    private final HeroSlideRepository heroSlideRepo;
    private final MarketingSendRepository marketingSendRepo;
    private final PageBlockRepository pageBlockRepo;
    private final ShiftRepository shiftRepo;
    private final SupplierRepository supplierRepo;
    private final SizeScaleRepository sizeScaleRepo;
    private final ExpenseRepository expenseRepo;
    private final StockMovementRepository stockMovementRepo;
    private final ExpenseBudgetRepository expenseBudgetRepo;
    private final SiteSettingsRepository siteSettingsRepo;
    private final MarketingConfigRepository marketingConfigRepo;

    public LimpiezaEcommerce(OrderRepository orderRepo, ExchangeRepository exchangeRepo, ParamRepository paramRepo,
                             ProductRepository productRepo, CouponRepository couponRepo,
                             DiscountRepository discountRepo, HeroSlideRepository heroSlideRepo,
                             MarketingSendRepository marketingSendRepo, PageBlockRepository pageBlockRepo,
                             ShiftRepository shiftRepo, SupplierRepository supplierRepo,
                             SizeScaleRepository sizeScaleRepo, ExpenseRepository expenseRepo,
                             StockMovementRepository stockMovementRepo, ExpenseBudgetRepository expenseBudgetRepo,
                             SiteSettingsRepository siteSettingsRepo, MarketingConfigRepository marketingConfigRepo) {
        this.orderRepo = orderRepo;
        this.exchangeRepo = exchangeRepo;
        this.paramRepo = paramRepo;
        this.productRepo = productRepo;
        this.couponRepo = couponRepo;
        this.discountRepo = discountRepo;
        this.heroSlideRepo = heroSlideRepo;
        this.marketingSendRepo = marketingSendRepo;
        this.pageBlockRepo = pageBlockRepo;
        this.shiftRepo = shiftRepo;
        this.supplierRepo = supplierRepo;
        this.sizeScaleRepo = sizeScaleRepo;
        this.expenseRepo = expenseRepo;
        this.stockMovementRepo = stockMovementRepo;
        this.expenseBudgetRepo = expenseBudgetRepo;
        this.siteSettingsRepo = siteSettingsRepo;
        this.marketingConfigRepo = marketingConfigRepo;
    }

    @Override
    public void borrarDatosDe(String tenantId) {
        orderRepo.deleteAllByTenantId(tenantId);
        exchangeRepo.deleteAllByTenantId(tenantId);
        paramRepo.deleteAllByTenantId(tenantId);
        productRepo.deleteAllByTenantId(tenantId);
        couponRepo.deleteAllByTenantId(tenantId);
        discountRepo.deleteAllByTenantId(tenantId);
        heroSlideRepo.deleteAllByTenantId(tenantId);
        marketingSendRepo.deleteAllByTenantId(tenantId);
        pageBlockRepo.deleteAllByTenantId(tenantId);
        shiftRepo.deleteAllByTenantId(tenantId);
        supplierRepo.deleteAllByTenantId(tenantId);
        sizeScaleRepo.deleteAllByTenantId(tenantId);
        expenseRepo.deleteAllByTenantId(tenantId);
        stockMovementRepo.deleteAllByTenantId(tenantId);
        expenseBudgetRepo.deleteAllByTenantId(tenantId);
        if (siteSettingsRepo.existsById(tenantId)) siteSettingsRepo.deleteById(tenantId);
        if (marketingConfigRepo.existsById(tenantId)) marketingConfigRepo.deleteById(tenantId);
    }
}
