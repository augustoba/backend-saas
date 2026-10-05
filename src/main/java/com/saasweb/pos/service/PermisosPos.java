package com.saasweb.pos.service;

import com.saasweb.core.model.PermisoDef;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.service.CatalogoPermisos;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Permisos del punto de venta. Hasta la Etapa 3 el kiosco usa las mismas
 * pantallas y claves que la venta en el local del ecommerce, por eso las
 * claves coinciden; cuando el POS tenga sus propias pantallas, éste es el
 * único lugar que cambia.
 */
@Component
public class PermisosPos implements CatalogoPermisos {

    @Override
    public Sistema sistema() {
        return Sistema.POS;
    }

    @Override
    public List<PermisoDef> permisos() {
        return List.of(
                PermisoDef.de("PRODUCTS_VIEW", "Ver artículos"),
                PermisoDef.de("PRODUCTS_MANAGE", "Gestionar artículos"),
                PermisoDef.de("POS_USE", "Vender"),
                PermisoDef.de("CASH_REGISTER_VIEW", "Ver la caja"),
                PermisoDef.de("SHIFTS_MANAGE", "Abrir y cerrar caja"),
                PermisoDef.de("SUPPLIERS_MANAGE", "Proveedores"),
                PermisoDef.de("METRICS_VIEW", "Métricas"),
                PermisoDef.de("EXPENSES_MANAGE", "Gestionar gastos"),
                PermisoDef.de("FINANCE_VIEW", "Ver balance de ganancias"),
                PermisoDef.de("STOCK_MOVEMENTS_VIEW", "Historial de movimientos de stock"));
    }

    @Override
    public Map<String, Set<String>> rolesPorDefecto() {
        return Map.of("Cajero", Set.of("POS_USE", "CASH_REGISTER_VIEW", "SHIFTS_MANAGE"));
    }
}
