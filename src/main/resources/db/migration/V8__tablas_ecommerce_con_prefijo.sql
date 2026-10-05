-- Etapa 2.2a: las tablas del ecommerce pasan a tener prefijo ec_ (ver el spec).
-- Sólo cambia el nombre: columnas, datos y claves foráneas quedan iguales.

RENAME TABLE
    `coupon` TO `ec_cupon`,
    `discount` TO `ec_descuento`,
    `exchange` TO `ec_cambio`,
    `exchange_line` TO `ec_cambio_linea`,
    `expense` TO `ec_gasto`,
    `expense_budget` TO `ec_presupuesto_gasto`,
    `hero_slide` TO `ec_carrusel`,
    `marketing_config` TO `ec_marketing_config`,
    `marketing_send` TO `ec_marketing_envio`,
    `orders` TO `ec_pedido`,
    `order_line` TO `ec_pedido_linea`,
    `page_block` TO `ec_bloque_pagina`,
    `param_group` TO `ec_param_grupo`,
    `param_option` TO `ec_param_opcion`,
    `product` TO `ec_producto`,
    `product_image` TO `ec_producto_imagen`,
    `product_size_stock` TO `ec_producto_talle_stock`,
    `product_param` TO `ec_producto_param`,
    `shift` TO `ec_turno`,
    `site_settings` TO `ec_config_tienda`,
    `size_scale` TO `ec_escala_talle`,
    `size_scale_value` TO `ec_escala_talle_valor`,
    `stock_movement` TO `ec_movimiento_stock`,
    `supplier` TO `ec_proveedor`;
