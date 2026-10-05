-- Etapa 4a: el restaurante (portado de C:\proyectos\restaurante) con tablas rs_.
-- Generado desde el mapeo JPA (Hibernate, MySQL) y revisado: enums como varchar (regla V3),
-- tenant_id = id de la tienda del núcleo, usuarios = ids del núcleo (sin FK).

create table `rs_arqueo` (`diferencia` decimal(38,2), `monto_apertura` decimal(38,2) not null, `monto_cierre_declarado` decimal(38,2), `monto_cierre_sistema` decimal(38,2), `total_ventas` decimal(38,2) not null, `caja_id` bigint not null, `fecha_apertura` datetime(6) not null, `fecha_cierre` datetime(6), `id` bigint not null auto_increment, `observaciones` varchar(255), `usuario_apertura_id` varchar(255) not null, `usuario_cierre_id` varchar(255), `estado` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_caja` (`activo` bit not null, `id` bigint not null auto_increment, `sucursal_id` bigint not null, `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_categoria` (`activo` bit not null, `orden` integer not null, `id` bigint not null auto_increment, `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_categoria_gasto` (`activo` bit not null, `clasificacion_gasto_id` bigint not null, `id` bigint not null auto_increment, `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_clasificacion_gasto` (`activo` bit not null, `id` bigint not null auto_increment, `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_cliente` (`activo` bit not null, `descuento_porcentaje` decimal(38,2), `limite_credito` decimal(38,2), `saldo_cuenta_corriente` decimal(38,2) not null, `tiene_cuenta_corriente` bit not null, `fecha_alta` datetime(6) not null, `id` bigint not null auto_increment, `cuit` varchar(255), `direccion` varchar(255), `email` varchar(255), `instagram` varchar(255), `nombre` varchar(255) not null, `notas` varchar(255), `telefono` varchar(255), `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_comentario_producto` (`activo` bit not null, `fecha` datetime(6) not null, `id` bigint not null auto_increment, `producto_id` bigint not null, `texto` varchar(500) not null, `nombre_cliente` varchar(255) not null, `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_config_reserva_salon` (`capacidad_reservable` integer not null, `id` bigint not null auto_increment, `salon_id` bigint not null, primary key (`id`)) engine=InnoDB;

create table `rs_cuenta_corriente_movimiento` (`fecha` date not null, `monto` decimal(38,2) not null, `saldo_resultante` decimal(38,2) not null, `cliente_id` bigint not null, `id` bigint not null auto_increment, `descripcion` varchar(255), `tenant_id` varchar(255) not null, `tipo` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_gasto` (`fecha` date not null, `fecha_pago` date, `fecha_vencimiento` date, `monto` decimal(38,2) not null, `categoria_gasto_id` bigint not null, `id` bigint not null auto_increment, `proveedor_id` bigint, `sucursal_id` bigint not null, `descripcion` varchar(255), `numero_comprobante` varchar(255), `tenant_id` varchar(255) not null, `estado` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_grupo_modificador` (`activo` bit not null, `max` integer, `min` integer, `multiple` bit not null, `obligatorio` bit not null, `id` bigint not null auto_increment, `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_lista_precio` (`activo` bit not null, `id` bigint not null auto_increment, `descripcion` varchar(255), `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_lista_precio_item` (`precio` decimal(38,2), `id` bigint not null auto_increment, `lista_precio_id` bigint not null, `producto_id` bigint not null, primary key (`id`)) engine=InnoDB;

create table `rs_medio_pago` (`activo` bit not null, `es_cuenta_corriente` bit not null, `es_efectivo` bit not null, `orden` integer not null, `requiere_referencia` bit not null, `id` bigint not null auto_increment, `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_menu_online_config` (`activo` bit not null, `columnas_max` integer not null, `mostrar_descripciones` bit not null, `mostrar_direccion` bit not null, `id` bigint not null auto_increment, `sucursal_id` bigint not null, `encabezado_texto` varchar(2000), `banner_url` varchar(255), `logo_url` varchar(255), `slug` varchar(255) not null, `tenant_id` varchar(255) not null, `forma_logo` varchar(40) not null, `layout` varchar(40) not null, `tipografia` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_menu_online_red_social` (`orden` integer not null, `menu_online_config_id` bigint not null, `nombre_mostrar` varchar(255), `plataforma` varchar(255), `url` varchar(255), primary key (`orden`, `menu_online_config_id`)) engine=InnoDB;

create table `rs_mesa` (`alto` integer not null, `ancho` integer not null, `capacidad` integer not null, `pos_x` integer not null, `pos_y` integer not null, `rotacion` integer not null, `id` bigint not null auto_increment, `salon_id` bigint not null, `nombre` varchar(255) not null, `estado` varchar(40) not null, `forma` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_modificador` (`activo` bit not null, `precio_adicional` decimal(38,2) not null, `grupo_modificador_id` bigint not null, `id` bigint not null auto_increment, `nombre` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_movimiento_caja` (`monto` decimal(38,2) not null, `arqueo_id` bigint not null, `fecha` datetime(6) not null, `id` bigint not null auto_increment, `medio_pago_id` bigint, `descripcion` varchar(255), `usuario_id` varchar(255) not null, `tipo` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_movimiento_stock` (`cantidad` integer not null, `stock_resultante` integer not null, `fecha` datetime(6) not null, `id` bigint not null auto_increment, `producto_id` bigint not null, `motivo` varchar(255), `tenant_id` varchar(255) not null, `tipo` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_orden` (`comensales` integer, `costo_envio` decimal(38,2) not null, `descuento_monto` decimal(38,2) not null, `paso_actual` integer not null, `propina` decimal(38,2) not null, `subtotal` decimal(38,2) not null, `total` decimal(38,2) not null, `arqueo_id` bigint, `cliente_id` bigint, `fecha_confirmacion` datetime(6), `fecha_creacion` datetime(6) not null, `fecha_pago` datetime(6), `fecha_tomado` datetime(6), `id` bigint not null auto_increment, `mesa_id` bigint, `sucursal_id` bigint not null, `usuario_nombre` varchar(200), `comentario` varchar(255), `direccion_entrega` varchar(255), `nombre_cliente_online` varchar(255), `telefono_cliente_online` varchar(255), `tenant_id` varchar(255) not null, `token_sesion` varchar(255), `usuario_id` varchar(255), `estado` varchar(40) not null, `estado_preparacion` varchar(40) not null, `tipo` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_orden_item` (`cantidad` integer not null, `paso` integer not null, `precio_unitario` decimal(38,2) not null, `subtotal` decimal(38,2) not null, `id` bigint not null auto_increment, `orden_id` bigint not null, `producto_id` bigint not null, `notas` varchar(255), `estado` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_pago` (`monto` decimal(38,2) not null, `propina` decimal(38,2) not null, `fecha` datetime(6) not null, `id` bigint not null auto_increment, `medio_pago_id` bigint not null, `orden_id` bigint not null, `referencia` varchar(255), `usuario_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_preferencias` (`agrupar_por_producto` bit not null, `pasos_por_defecto` bit not null, `precios_con_centavos` bit not null, `sonidos_activos` bit not null, `id` bigint not null auto_increment, `sucursal_id` bigint not null, primary key (`id`)) engine=InnoDB;

create table `rs_producto` (`activo` bit not null, `controla_stock` bit not null, `costo` decimal(38,2), `disponible` bit not null, `disponible_online` bit not null, `es_favorito` bit not null, `es_recomendacion` bit not null, `likes` integer not null, `precio` decimal(38,2), `stock_actual` integer, `vistas` integer not null, `categoria_id` bigint not null, `id` bigint not null auto_increment, `codigo` varchar(255), `descripcion` varchar(255), `imagen_url` varchar(255), `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, `unidad` varchar(255), `tipo` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_proveedor` (`activo` bit not null, `saldo` decimal(38,2) not null, `id` bigint not null auto_increment, `cuit` varchar(255), `direccion` varchar(255), `email` varchar(255), `nombre` varchar(255) not null, `telefono` varchar(255), `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_reserva` (`cantidad_personas` integer not null, `fecha` date not null, `hora` time(6), `cliente_id` bigint, `fecha_creacion` datetime(6) not null, `id` bigint not null auto_increment, `salon_id` bigint not null, `sucursal_id` bigint not null, `nombre_cliente` varchar(255), `notas` varchar(255), `telefono_cliente` varchar(255), `tenant_id` varchar(255) not null, `estado` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_salon` (`orden` integer not null, `id` bigint not null auto_increment, `sucursal_id` bigint not null, `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_sector` (`activo` bit not null, `kds_activo` bit not null, `id` bigint not null auto_increment, `sucursal_id` bigint not null, `impresora_ip` varchar(255), `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, `tipo` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_solicitud_mesa` (`fecha` datetime(6) not null, `id` bigint not null auto_increment, `orden_id` bigint not null, `tenant_id` varchar(255) not null, `estado` varchar(40) not null, `tipo` varchar(40) not null, primary key (`id`)) engine=InnoDB;

create table `rs_sucursal` (`activo` bit not null, `id` bigint not null auto_increment, `direccion` varchar(255), `moneda` varchar(255), `nombre` varchar(255) not null, `telefono` varchar(255), `tenant_id` varchar(255) not null, `zona_horaria` varchar(255), primary key (`id`)) engine=InnoDB;

create table `rs_turno` (`activo` bit not null, `hora_fin` time(6) not null, `hora_inicio` time(6) not null, `id` bigint not null auto_increment, `sucursal_id` bigint not null, `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_turno_reserva` (`activo` bit not null, `capacidad_maxima` integer not null, `hora_fin` time(6) not null, `hora_inicio` time(6) not null, `id` bigint not null auto_increment, `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

create table `rs_zona_envio` (`activo` bit not null, `costo` decimal(38,2) not null, `id` bigint not null auto_increment, `nombre` varchar(255) not null, `tenant_id` varchar(255) not null, primary key (`id`)) engine=InnoDB;

alter table `rs_config_reserva_salon` add constraint `UKm3sxupegvnyb7yfxki9mcxv5d` unique (`salon_id`);

alter table `rs_lista_precio_item` add constraint uk_lista_precio_item unique (`lista_precio_id`, `producto_id`);

alter table `rs_menu_online_config` add constraint `UKfbn8rkv5nc1cx2nr84nxkbmaj` unique (`slug`);

alter table `rs_orden` add constraint `UKbxuqvfesyugv9ks4i67l2l71h` unique (`token_sesion`);

alter table `rs_preferencias` add constraint `UKgmdl214jm7y6c8i5ax3p9dlty` unique (`sucursal_id`);

alter table `rs_arqueo` add constraint `FKdwtgbbettrxuxrqs4q6doh7yi` foreign key (`caja_id`) references `rs_caja` (`id`);

alter table `rs_categoria_gasto` add constraint `FK9lc0450xp363deaf2n3bxju1t` foreign key (`clasificacion_gasto_id`) references `rs_clasificacion_gasto` (`id`);

alter table `rs_comentario_producto` add constraint `FKdmhe3hq28eflgou54xa0hd5aa` foreign key (`producto_id`) references `rs_producto` (`id`);

alter table `rs_cuenta_corriente_movimiento` add constraint `FK5uvp5lofgj8gmf7ejujlchs56` foreign key (`cliente_id`) references `rs_cliente` (`id`);

alter table `rs_gasto` add constraint `FKd2ngrkk3t5hfon59aqei0rw79` foreign key (`categoria_gasto_id`) references `rs_categoria_gasto` (`id`);

alter table `rs_gasto` add constraint `FKe4o99m96wgeru43skbnno6gnc` foreign key (`proveedor_id`) references `rs_proveedor` (`id`);

alter table `rs_lista_precio_item` add constraint `FK5q8p55tqr01qjraaw7q3dwirg` foreign key (`lista_precio_id`) references `rs_lista_precio` (`id`);

alter table `rs_lista_precio_item` add constraint `FK7paf5cwkufkdc95tg0iddg2jt` foreign key (`producto_id`) references `rs_producto` (`id`);

alter table `rs_menu_online_red_social` add constraint `FKrnmmdavs0kgxyx0nuag71turm` foreign key (`menu_online_config_id`) references `rs_menu_online_config` (`id`);

alter table `rs_mesa` add constraint `FKe2wsywmixmwuils747vd5smvp` foreign key (`salon_id`) references `rs_salon` (`id`);

alter table `rs_modificador` add constraint `FK1wy7ogua8l06uhj7qybtx7m5o` foreign key (`grupo_modificador_id`) references `rs_grupo_modificador` (`id`);

alter table `rs_movimiento_caja` add constraint `FKqpma8gl6qjk4fo6m1qjt0qvre` foreign key (`arqueo_id`) references `rs_arqueo` (`id`);

alter table `rs_movimiento_stock` add constraint `FKiu79erslt8p9jkjgoi146hcdy` foreign key (`producto_id`) references `rs_producto` (`id`);

alter table `rs_orden` add constraint `FKpm4bsrws2fwoxib3riqeayhuu` foreign key (`arqueo_id`) references `rs_arqueo` (`id`);

alter table `rs_orden` add constraint `FKjmqlfq9ylow0to4hk6l345ajk` foreign key (`cliente_id`) references `rs_cliente` (`id`);

alter table `rs_orden` add constraint `FKrhhj5gfpjk186e5iy1hdcfg8g` foreign key (`mesa_id`) references `rs_mesa` (`id`);

alter table `rs_orden_item` add constraint `FK4k23bw9s1pls3w8at9xk9h77s` foreign key (`orden_id`) references `rs_orden` (`id`);

alter table `rs_orden_item` add constraint `FKlixnuihnf5oghss6r8tottaih` foreign key (`producto_id`) references `rs_producto` (`id`);

alter table `rs_pago` add constraint `FK9jr0awl0mnngf98ph4c6ou6ui` foreign key (`medio_pago_id`) references `rs_medio_pago` (`id`);

alter table `rs_pago` add constraint `FKdjxqed1hol4r1gk10ylsi9hp5` foreign key (`orden_id`) references `rs_orden` (`id`);

alter table `rs_producto` add constraint `FK2sltqnfpgwsper7cxpglaa4ab` foreign key (`categoria_id`) references `rs_categoria` (`id`);

alter table `rs_reserva` add constraint `FKs0hje9lfajh15qqhisc2djbay` foreign key (`salon_id`) references `rs_salon` (`id`);

alter table `rs_solicitud_mesa` add constraint `FKkvtoaq4fcdevctcq8lms7cl45` foreign key (`orden_id`) references `rs_orden` (`id`);

-- Todas las consultas filtran por tienda.
create index `idx_rs_caja_tenant` on `rs_caja` (`tenant_id`);
create index `idx_rs_categoria_tenant` on `rs_categoria` (`tenant_id`);
create index `idx_rs_categoria_gasto_tenant` on `rs_categoria_gasto` (`tenant_id`);
create index `idx_rs_clasificacion_gasto_tenant` on `rs_clasificacion_gasto` (`tenant_id`);
create index `idx_rs_cliente_tenant` on `rs_cliente` (`tenant_id`);
create index `idx_rs_comentario_producto_tenant` on `rs_comentario_producto` (`tenant_id`);
create index `idx_rs_cuenta_corriente_movimiento_tenant` on `rs_cuenta_corriente_movimiento` (`tenant_id`);
create index `idx_rs_gasto_tenant` on `rs_gasto` (`tenant_id`);
create index `idx_rs_grupo_modificador_tenant` on `rs_grupo_modificador` (`tenant_id`);
create index `idx_rs_lista_precio_tenant` on `rs_lista_precio` (`tenant_id`);
create index `idx_rs_medio_pago_tenant` on `rs_medio_pago` (`tenant_id`);
create index `idx_rs_menu_online_config_tenant` on `rs_menu_online_config` (`tenant_id`);
create index `idx_rs_movimiento_stock_tenant` on `rs_movimiento_stock` (`tenant_id`);
create index `idx_rs_orden_tenant` on `rs_orden` (`tenant_id`);
create index `idx_rs_producto_tenant` on `rs_producto` (`tenant_id`);
create index `idx_rs_proveedor_tenant` on `rs_proveedor` (`tenant_id`);
create index `idx_rs_reserva_tenant` on `rs_reserva` (`tenant_id`);
create index `idx_rs_salon_tenant` on `rs_salon` (`tenant_id`);
create index `idx_rs_sector_tenant` on `rs_sector` (`tenant_id`);
create index `idx_rs_solicitud_mesa_tenant` on `rs_solicitud_mesa` (`tenant_id`);
create index `idx_rs_sucursal_tenant` on `rs_sucursal` (`tenant_id`);
create index `idx_rs_turno_tenant` on `rs_turno` (`tenant_id`);
create index `idx_rs_turno_reserva_tenant` on `rs_turno_reserva` (`tenant_id`);
create index `idx_rs_zona_envio_tenant` on `rs_zona_envio` (`tenant_id`);
