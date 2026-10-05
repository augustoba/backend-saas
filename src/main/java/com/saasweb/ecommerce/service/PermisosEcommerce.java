package com.saasweb.ecommerce.service;

import com.saasweb.core.model.PermisoDef;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.service.CatalogoPermisos;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Permisos del panel del ecommerce y sus roles por defecto. */
@Component
public class PermisosEcommerce implements CatalogoPermisos {

    @Override
    public Sistema sistema() {
        return Sistema.ECOMMERCE;
    }

    @Override
    public List<PermisoDef> permisos() {
        return List.of(
                PermisoDef.de("PRODUCTS_VIEW", "Ver productos"),
                PermisoDef.de("PRODUCTS_MANAGE", "Gestionar productos"),
                PermisoDef.de("ORDERS_VIEW", "Ver pedidos"),
                PermisoDef.de("ORDERS_MANAGE", "Gestionar pedidos"),
                PermisoDef.de("POS_USE", "Registrar ventas en el local"),
                PermisoDef.de("EXCHANGES_USE", "Registrar cambios de prenda"),
                PermisoDef.de("CASH_REGISTER_VIEW", "Ver la caja (cierre del día)"),
                PermisoDef.de("SHIFTS_MANAGE", "Turnos"),
                PermisoDef.de("PARAMS_MANAGE", "Parametrías"),
                PermisoDef.de("SIZE_SCALES_MANAGE", "Escalas de talle"),
                PermisoDef.de("SUPPLIERS_MANAGE", "Proveedores"),
                PermisoDef.de("DISCOUNTS_MANAGE", "Descuentos"),
                PermisoDef.de("COUPONS_MANAGE", "Cupones"),
                PermisoDef.de("MARKETING_MANAGE", "Marketing"),
                PermisoDef.de("METRICS_VIEW", "Métricas"),
                // El carrusel quedó como identidad de la tienda: lo maneja el superadmin.
                new PermisoDef("CAROUSEL_MANAGE", "Carrusel", true),
                PermisoDef.de("EXPENSES_MANAGE", "Gestionar gastos"),
                PermisoDef.de("FINANCE_VIEW", "Ver balance de ganancias"),
                PermisoDef.de("STOCK_MOVEMENTS_VIEW", "Historial de movimientos de stock"));
    }

    @Override
    public Map<String, Set<String>> rolesPorDefecto() {
        return Map.of("Vendedor", Set.of("ORDERS_VIEW", "ORDERS_MANAGE", "POS_USE", "EXCHANGES_USE",
                "CASH_REGISTER_VIEW", "SHIFTS_MANAGE", "METRICS_VIEW"));
    }
}
