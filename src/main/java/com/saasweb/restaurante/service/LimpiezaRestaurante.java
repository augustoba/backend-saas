package com.saasweb.restaurante.service;

import com.saasweb.core.service.LimpiezaDeTienda;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Borra todo lo que el restaurante guardó para una tienda (baja definitiva).
 * Con SQL en orden de dependencias: varias tablas hijas (mesas, ítems, pagos,
 * arqueos...) no tienen tenant_id propio y se alcanzan por su padre.
 */
@Component
public class LimpiezaRestaurante implements LimpiezaDeTienda {

    private static final String[] BORRADOS = {
            "delete from rs_solicitud_mesa where orden_id in (select id from rs_orden where tenant_id = ?)",
            "delete from rs_orden_item where orden_id in (select id from rs_orden where tenant_id = ?)",
            "delete from rs_pago where orden_id in (select id from rs_orden where tenant_id = ?)",
            "delete from rs_orden where tenant_id = ?",
            "delete from rs_movimiento_caja where arqueo_id in (select a.id from rs_arqueo a join rs_caja c on c.id = a.caja_id where c.tenant_id = ?)",
            "delete from rs_arqueo where caja_id in (select id from rs_caja where tenant_id = ?)",
            "delete from rs_caja where tenant_id = ?",
            "delete from rs_reserva where tenant_id = ?",
            "delete from rs_config_reserva_salon where salon_id in (select id from rs_salon where tenant_id = ?)",
            "delete from rs_mesa where salon_id in (select id from rs_salon where tenant_id = ?)",
            "delete from rs_salon where tenant_id = ?",
            "delete from rs_gasto where tenant_id = ?",
            "delete from rs_categoria_gasto where tenant_id = ?",
            "delete from rs_clasificacion_gasto where tenant_id = ?",
            "delete from rs_proveedor where tenant_id = ?",
            "delete from rs_cuenta_corriente_movimiento where tenant_id = ?",
            "delete from rs_cliente where tenant_id = ?",
            "delete from rs_lista_precio_item where lista_precio_id in (select id from rs_lista_precio where tenant_id = ?)",
            "delete from rs_lista_precio where tenant_id = ?",
            "delete from rs_comentario_producto where tenant_id = ?",
            "delete from rs_producto_grupo_modificador where producto_id in (select id from rs_producto where tenant_id = ?)",
            "delete from rs_movimiento_stock where tenant_id = ?",
            "delete from rs_producto where tenant_id = ?",
            "delete from rs_categoria where tenant_id = ?",
            "delete from rs_modificador where grupo_modificador_id in (select id from rs_grupo_modificador where tenant_id = ?)",
            "delete from rs_grupo_modificador where tenant_id = ?",
            "delete from rs_menu_online_red_social where menu_online_config_id in (select id from rs_menu_online_config where tenant_id = ?)",
            "delete from rs_menu_online_config where tenant_id = ?",
            "delete from rs_medio_pago where tenant_id = ?",
            "delete from rs_zona_envio where tenant_id = ?",
            "delete from rs_turno_reserva where tenant_id = ?",
            "delete from rs_turno where tenant_id = ?",
            "delete from rs_sector where tenant_id = ?",
            "delete from rs_preferencias where sucursal_id in (select id from rs_sucursal where tenant_id = ?)",
            "delete from rs_sucursal where tenant_id = ?",
    };

    private final JdbcTemplate jdbc;

    public LimpiezaRestaurante(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void borrarDatosDe(String tenantId) {
        for (String sql : BORRADOS) jdbc.update(sql, tenantId);
    }
}
