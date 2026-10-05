package com.saasweb.restaurante.service;

import com.saasweb.core.model.PermisoDef;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.service.CatalogoPermisos;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Permisos del restaurante (el catálogo del proyecto original, salvo usuarios y
 * roles, que son del núcleo). Roles por defecto como los del original:
 * encargado, cajero, recepción y mozo; el "Administrador" los tiene todos.
 */
@Component
public class PermisosRestaurante implements CatalogoPermisos {

    @Override
    public Sistema sistema() {
        return Sistema.RESTAURANTE;
    }

    @Override
    public List<PermisoDef> permisos() {
        return List.of(
                PermisoDef.de("MESA_ABRIR", "Abrir mesas"),
                PermisoDef.de("MESA_CERRAR", "Cerrar mesas"),
                PermisoDef.de("MESA_ASIGNAR_MESERO", "Asignar mozo a una mesa"),
                PermisoDef.de("ORDEN_CREAR", "Tomar pedidos"),
                PermisoDef.de("ORDEN_CANCELAR", "Cancelar pedidos"),
                PermisoDef.de("ORDEN_COBRAR", "Cobrar"),
                PermisoDef.de("CAJA_ABRIR", "Abrir caja"),
                PermisoDef.de("CAJA_CERRAR", "Cerrar arqueos"),
                PermisoDef.de("CAJA_VER_ARQUEO", "Ver arqueos"),
                PermisoDef.de("PRODUCTO_CREAR", "Crear productos"),
                PermisoDef.de("PRODUCTO_EDITAR", "Editar productos"),
                PermisoDef.de("PRODUCTO_ACTUALIZAR_PRECIO", "Actualizar precios"),
                PermisoDef.de("PRODUCTO_MODIFICADORES_GESTIONAR", "Modificadores"),
                PermisoDef.de("PRODUCTO_LISTAS_PRECIO_GESTIONAR", "Listas de precio"),
                PermisoDef.de("PRODUCTO_STOCK_GESTIONAR", "Stock e inventario"),
                PermisoDef.de("CLIENTE_VER", "Ver clientes"),
                PermisoDef.de("CLIENTE_CREAR", "Crear clientes"),
                PermisoDef.de("CUENTA_CORRIENTE_VER", "Cuentas corrientes"),
                PermisoDef.de("RESERVA_VER", "Ver reservas"),
                PermisoDef.de("RESERVA_CREAR", "Crear reservas"),
                PermisoDef.de("GASTO_VER", "Ver gastos"),
                PermisoDef.de("GASTO_CREAR", "Cargar gastos"),
                PermisoDef.de("PROVEEDOR_VER", "Ver proveedores"),
                PermisoDef.de("PROVEEDOR_CREAR", "Crear proveedores"),
                PermisoDef.de("REPORTE_VENTAS", "Reporte de ventas"),
                PermisoDef.de("REPORTE_PYL", "Finanzas"),
                PermisoDef.de("REPORTE_PROPINAS", "Reporte de propinas"),
                PermisoDef.de("ADMIN_CONFIGURAR", "Configurar el sistema"),
                PermisoDef.de("MENU_ONLINE_CONFIGURAR", "Configurar la carta online"),
                PermisoDef.de("KDS_OPERAR", "Cocina (KDS)"),
                PermisoDef.de("SOLICITUD_MESA_ATENDER", "Atender pedidos de las mesas"));
    }

    @Override
    public Map<String, Set<String>> rolesPorDefecto() {
        return Map.of(
                "Encargado", Set.of("CAJA_ABRIR", "CAJA_CERRAR", "CAJA_VER_ARQUEO", "ORDEN_COBRAR", "GASTO_VER",
                        "GASTO_CREAR", "PROVEEDOR_VER", "PROVEEDOR_CREAR", "REPORTE_VENTAS", "REPORTE_PYL",
                        "REPORTE_PROPINAS", "PRODUCTO_CREAR", "PRODUCTO_EDITAR", "PRODUCTO_ACTUALIZAR_PRECIO",
                        "PRODUCTO_MODIFICADORES_GESTIONAR", "PRODUCTO_LISTAS_PRECIO_GESTIONAR", "PRODUCTO_STOCK_GESTIONAR",
                        "KDS_OPERAR", "SOLICITUD_MESA_ATENDER", "CLIENTE_VER", "CLIENTE_CREAR", "CUENTA_CORRIENTE_VER",
                        "RESERVA_VER", "RESERVA_CREAR", "MESA_ABRIR", "MESA_CERRAR", "MESA_ASIGNAR_MESERO", "ORDEN_CREAR"),
                "Cajero", Set.of("MESA_ABRIR", "MESA_CERRAR", "MESA_ASIGNAR_MESERO", "ORDEN_CREAR", "ORDEN_COBRAR",
                        "CAJA_ABRIR", "CAJA_CERRAR", "CAJA_VER_ARQUEO", "CLIENTE_VER"),
                "Recepción", Set.of("MESA_ABRIR", "RESERVA_VER", "RESERVA_CREAR", "CLIENTE_VER"),
                "Mozo", Set.of("MESA_ABRIR", "ORDEN_CREAR", "ORDEN_CANCELAR", "KDS_OPERAR", "SOLICITUD_MESA_ATENDER"));
    }
}
