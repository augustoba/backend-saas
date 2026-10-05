-- Etapa 3a: el POS con tablas propias (artículos, categorías y movimientos de stock).

CREATE TABLE `pos_categoria` (
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `posicion` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_pos_categoria_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pos_articulo` (
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `nombre` varchar(200) NOT NULL,
  `codigo_barras` varchar(64) DEFAULT NULL,
  `precio` decimal(12,2) NOT NULL,
  `costo` decimal(12,2) DEFAULT NULL,
  `iva` decimal(5,2) DEFAULT NULL,
  `stock` int NOT NULL DEFAULT 0,
  `umbral_stock` int DEFAULT NULL,
  `categoria_id` varchar(255) DEFAULT NULL,
  `archivado` bit(1) NOT NULL DEFAULT b'0',
  `creado_en` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pos_articulo_codigo` (`tenant_id`, `codigo_barras`),
  KEY `idx_pos_articulo_tenant` (`tenant_id`, `archivado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pos_movimiento_stock` (
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `articulo_id` varchar(255) NOT NULL,
  `articulo_nombre` varchar(200) NOT NULL,
  `cantidad` int NOT NULL,
  `stock_resultante` int NOT NULL,
  `motivo` varchar(20) NOT NULL,
  `referencia_id` varchar(255) DEFAULT NULL,
  `nota` varchar(500) DEFAULT NULL,
  `costo_unitario` decimal(12,2) DEFAULT NULL,
  `autor_dni` varchar(20) DEFAULT NULL,
  `autor_nombre` varchar(200) DEFAULT NULL,
  `fecha` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pos_mov_stock_tenant` (`tenant_id`, `fecha`),
  KEY `idx_pos_mov_stock_articulo` (`articulo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
