-- Etapa 2.2b: el talle embebido en el producto pasa a ser una variante con
-- identidad propia (id) y código de barras opcional. Sirve para cualquier
-- rubro: talle, medida, color… Las escalas de talle pasan a ser escalas de
-- variantes.

CREATE TABLE `ec_variante` (
  `id` varchar(255) NOT NULL,
  `product_id` varchar(255) NOT NULL,
  `posicion` int DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `stock` int NOT NULL,
  `codigo_barras` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_ec_variante_producto` (`product_id`),
  CONSTRAINT `fk_ec_variante_producto` FOREIGN KEY (`product_id`) REFERENCES `ec_producto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `ec_variante` (`id`, `product_id`, `posicion`, `nombre`, `stock`)
SELECT UUID(), `product_id`, `idx`, `size_value`, `stock` FROM `ec_producto_talle_stock`;

DROP TABLE `ec_producto_talle_stock`;

RENAME TABLE
    `ec_escala_talle` TO `ec_escala_variante`,
    `ec_escala_talle_valor` TO `ec_escala_variante_valor`;
