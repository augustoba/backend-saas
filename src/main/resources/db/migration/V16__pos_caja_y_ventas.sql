-- Etapa 3b: caja (turno con arqueo) y ventas del POS.

CREATE TABLE `pos_caja` (
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `numero` int NOT NULL,
  `estado` varchar(20) NOT NULL,
  `abierta_en` datetime(6) NOT NULL,
  `abierta_por_dni` varchar(20) DEFAULT NULL,
  `abierta_por_nombre` varchar(200) DEFAULT NULL,
  `monto_inicial` decimal(12,2) NOT NULL,
  `cerrada_en` datetime(6) DEFAULT NULL,
  `cerrada_por_dni` varchar(20) DEFAULT NULL,
  `cerrada_por_nombre` varchar(200) DEFAULT NULL,
  `efectivo_esperado` decimal(12,2) DEFAULT NULL,
  `efectivo_contado` decimal(12,2) DEFAULT NULL,
  `diferencia` decimal(12,2) DEFAULT NULL,
  `nota_cierre` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pos_caja_numero` (`tenant_id`, `numero`),
  KEY `idx_pos_caja_estado` (`tenant_id`, `estado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pos_movimiento_caja` (
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `caja_id` varchar(255) NOT NULL,
  `tipo` varchar(20) NOT NULL,
  `monto` decimal(12,2) NOT NULL,
  `motivo` varchar(300) NOT NULL,
  `gasto_id` varchar(255) DEFAULT NULL,
  `autor_dni` varchar(20) DEFAULT NULL,
  `autor_nombre` varchar(200) DEFAULT NULL,
  `fecha` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pos_mov_caja` (`tenant_id`, `caja_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pos_venta` (
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `numero` int NOT NULL,
  `caja_id` varchar(255) NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `cajero_dni` varchar(20) DEFAULT NULL,
  `cajero_nombre` varchar(200) DEFAULT NULL,
  `total` decimal(12,2) NOT NULL,
  `medio_pago` varchar(20) NOT NULL,
  `monto_entregado` decimal(12,2) DEFAULT NULL,
  `vuelto` decimal(12,2) DEFAULT NULL,
  `referencia` varchar(200) DEFAULT NULL,
  `cuit_comprador` varchar(20) DEFAULT NULL,
  `estado` varchar(20) NOT NULL,
  `anulada_en` datetime(6) DEFAULT NULL,
  `anulada_por_nombre` varchar(200) DEFAULT NULL,
  `motivo_anulacion` varchar(300) DEFAULT NULL,
  `comprobante_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pos_venta_numero` (`tenant_id`, `numero`),
  KEY `idx_pos_venta_caja` (`tenant_id`, `caja_id`),
  KEY `idx_pos_venta_fecha` (`tenant_id`, `fecha`),
  CONSTRAINT `fk_pos_venta_comprobante` FOREIGN KEY (`comprobante_id`) REFERENCES `core_comprobante` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pos_venta_linea` (
  `id` varchar(255) NOT NULL,
  `venta_id` varchar(255) NOT NULL,
  `posicion` int DEFAULT NULL,
  `articulo_id` varchar(255) DEFAULT NULL,
  `nombre` varchar(200) NOT NULL,
  `precio_unitario` decimal(12,2) NOT NULL,
  `cantidad` int NOT NULL,
  `iva` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pos_venta_linea_venta` (`venta_id`),
  CONSTRAINT `fk_pos_venta_linea_venta` FOREIGN KEY (`venta_id`) REFERENCES `pos_venta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
