-- Etapa 3c: gastos y proveedores del POS; costo del artículo en la línea de venta (para la ganancia).

CREATE TABLE `pos_proveedor` (
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `nombre` varchar(200) NOT NULL,
  `telefono` varchar(50) DEFAULT NULL,
  `nota` varchar(1000) DEFAULT NULL,
  `creado_en` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pos_proveedor_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pos_gasto` (
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `fecha` date NOT NULL,
  `concepto` varchar(200) NOT NULL,
  `categoria` varchar(100) DEFAULT NULL,
  `monto` decimal(12,2) NOT NULL,
  `proveedor_id` varchar(255) DEFAULT NULL,
  `movimiento_caja_id` varchar(255) DEFAULT NULL,
  `autor_nombre` varchar(200) DEFAULT NULL,
  `creado_en` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pos_gasto_fecha` (`tenant_id`, `fecha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `pos_venta_linea` ADD COLUMN `costo_unitario` decimal(12,2) DEFAULT NULL;
