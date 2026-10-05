-- Etapa 1f: comprobantes fiscales en el núcleo, con su origen (sistema + id del
-- documento que los originó). Las notas de crédito se mudan acá; las facturas
-- de los pedidos se mudan en la Etapa 2, junto con el pedido (ec_pedido).

CREATE TABLE `core_comprobante` (
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `origen_sistema` varchar(20) NOT NULL,
  `origen_id` varchar(255) NOT NULL,
  `clase` varchar(20) NOT NULL,
  `tipo` varchar(20) NOT NULL,
  `importe` decimal(12,2) DEFAULT NULL,
  `motivo` varchar(500) DEFAULT NULL,
  `cuit_comprador` varchar(20) DEFAULT NULL,
  `cae` varchar(255) DEFAULT NULL,
  `cae_vencimiento` varchar(10) DEFAULT NULL,
  `numero` bigint DEFAULT NULL,
  `punto_venta` int DEFAULT NULL,
  `qr_url` varchar(500) DEFAULT NULL,
  `error` varchar(500) DEFAULT NULL,
  `created_by_dni` varchar(255) DEFAULT NULL,
  `created_by_name` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_core_comprobante_origen` (`tenant_id`, `origen_sistema`, `origen_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `core_comprobante` (`id`, `tenant_id`, `origen_sistema`, `origen_id`, `clase`, `tipo`, `importe`,
                                `motivo`, `cuit_comprador`, `cae`, `cae_vencimiento`, `numero`, `punto_venta`,
                                `qr_url`, `error`, `created_by_dni`, `created_by_name`, `created_at`)
SELECT cn.`id`, cn.`tenant_id`, 'ECOMMERCE', cn.`order_id`, 'NOTA_CREDITO', cn.`type`, cn.`amount`,
       cn.`reason`, o.`invoice_buyer_cuit`, cn.`cae`, cn.`cae_vencimiento`, cn.`number`, cn.`punto_venta`,
       cn.`qr_url`, cn.`error`, cn.`created_by_dni`, cn.`created_by_name`, cn.`created_at`
FROM `credit_note` cn
LEFT JOIN `orders` o ON o.`id` = cn.`order_id`;

DROP TABLE `credit_note`;
