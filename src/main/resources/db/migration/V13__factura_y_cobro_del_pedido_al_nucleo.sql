-- Etapa 2.3b: la factura y el cobro de Mercado Pago del pedido pasan al núcleo.
-- El pedido apunta a un core_comprobante y a un core_cobro_mp (sin guardar
-- copias de sus datos).

CREATE TABLE `core_cobro_mp` (
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `origen_sistema` varchar(20) NOT NULL,
  `origen_id` varchar(255) NOT NULL,
  `preference_id` varchar(100) DEFAULT NULL,
  `checkout_url` varchar(500) DEFAULT NULL,
  `payment_id` varchar(100) DEFAULT NULL,
  `estado` varchar(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_core_cobro_mp_origen` (`tenant_id`, `origen_sistema`, `origen_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `ec_pedido`
  ADD COLUMN `comprobante_id` varchar(255) DEFAULT NULL,
  ADD COLUMN `cobro_id` varchar(255) DEFAULT NULL;

-- Cobros: uno por pedido que llegó a crear la preferencia de Mercado Pago.
-- Su id es el del pedido: las preferencias viejas usaban el id del pedido como
-- referencia externa, así sus notificaciones siguen encontrando el cobro.
INSERT INTO `core_cobro_mp` (`id`, `tenant_id`, `origen_sistema`, `origen_id`, `preference_id`, `checkout_url`,
                             `payment_id`, `estado`, `created_at`)
SELECT `id`, `tenant_id`, 'ECOMMERCE', `id`, `mp_preference_id`, `mp_checkout_url`, `mp_payment_id`,
       COALESCE(`payment_status`, 'PENDING'), `created_at`
FROM `ec_pedido`
WHERE `mp_preference_id` IS NOT NULL OR `payment_status` IS NOT NULL;

UPDATE `ec_pedido` SET `cobro_id` = `id` WHERE `mp_preference_id` IS NOT NULL OR `payment_status` IS NOT NULL;

-- Facturas: aprobadas (con CAE) o intentos rechazados (con error).
INSERT INTO `core_comprobante` (`id`, `tenant_id`, `origen_sistema`, `origen_id`, `clase`, `tipo`, `importe`,
                                `cuit_comprador`, `cae`, `cae_vencimiento`, `numero`, `punto_venta`, `qr_url`,
                                `error`, `created_by_dni`, `created_by_name`, `created_at`)
SELECT CONCAT('fac-', `id`), `tenant_id`, 'ECOMMERCE', `id`, 'FACTURA',
       CASE WHEN `invoice_cae` IS NOT NULL THEN `invoice_type` ELSE 'FACTURA' END,
       `total`, `invoice_buyer_cuit`, `invoice_cae`, `invoice_cae_vencimiento`, `invoice_number`,
       `invoice_punto_venta`, `invoice_qr_url`,
       CASE WHEN `invoice_cae` IS NULL THEN `invoice_error` END,
       `confirmed_by_dni`, `confirmed_by_name`, COALESCE(`processed_at`, `created_at`)
FROM `ec_pedido`
WHERE `invoice_cae` IS NOT NULL OR `invoice_error` IS NOT NULL;

UPDATE `ec_pedido` SET `comprobante_id` = CONCAT('fac-', `id`)
WHERE `invoice_cae` IS NOT NULL OR `invoice_error` IS NOT NULL;

ALTER TABLE `ec_pedido`
  DROP COLUMN `invoice_type`, DROP COLUMN `invoice_cae`, DROP COLUMN `invoice_cae_vencimiento`,
  DROP COLUMN `invoice_number`, DROP COLUMN `invoice_punto_venta`, DROP COLUMN `invoice_qr_url`,
  DROP COLUMN `invoice_error`, DROP COLUMN `payment_status`, DROP COLUMN `mp_preference_id`,
  DROP COLUMN `mp_checkout_url`, DROP COLUMN `mp_payment_id`;
