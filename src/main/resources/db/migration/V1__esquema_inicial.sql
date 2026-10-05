-- V1: esquema previo a la reestructuración, tal como lo generaba Hibernate
-- (ddl-auto) al 2026-10-04. Punto de partida de Flyway; las etapas de la
-- reestructuración agregan V2+ creando las tablas core_/ec_/pos_/rs_.
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `admin_user` (
  `enabled` bit(1) NOT NULL,
  `super_admin` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `reset_token_expires_at` datetime(6) DEFAULT NULL,
  `dni` varchar(20) NOT NULL,
  `apellido` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `id` varchar(255) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `reset_token_hash` varchar(255) DEFAULT NULL,
  `role_id` varchar(255) DEFAULT NULL,
  `tenant_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK2fppylx5x3bxr0ami7gjedxjk` (`tenant_id`,`dni`),
  UNIQUE KEY `UKt9d60f8ofy3lpvpaw30cmt95k` (`tenant_id`,`email`),
  KEY `FKg34jw9lyxj9gcdlv3c767b4kc` (`role_id`),
  CONSTRAINT `FKg34jw9lyxj9gcdlv3c767b4kc` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `coupon` (
  `enabled` bit(1) NOT NULL,
  `expires_at` date DEFAULT NULL,
  `max_uses` int DEFAULT NULL,
  `min_amount` decimal(12,2) DEFAULT NULL,
  `stackable` bit(1) NOT NULL,
  `used_count` int NOT NULL,
  `value` decimal(12,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `code` varchar(40) NOT NULL,
  `label` varchar(200) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `kind` enum('AMOUNT','PERCENT') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKna7ub8ap953lmogwpsis636th` (`tenant_id`,`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `credit_note` (
  `amount` decimal(12,2) NOT NULL,
  `punto_venta` int DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `number` bigint DEFAULT NULL,
  `cae_vencimiento` varchar(10) DEFAULT NULL,
  `type` varchar(20) NOT NULL,
  `error` varchar(500) DEFAULT NULL,
  `qr_url` varchar(500) DEFAULT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `cae` varchar(255) DEFAULT NULL,
  `created_by_dni` varchar(255) DEFAULT NULL,
  `created_by_name` varchar(255) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  `order_id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `discount` (
  `discount_percent` int NOT NULL,
  `enabled` bit(1) NOT NULL,
  `ends_at` date DEFAULT NULL,
  `min_amount` decimal(38,2) DEFAULT NULL,
  `stackable` bit(1) NOT NULL,
  `starts_at` date DEFAULT NULL,
  `payment_methods` varchar(100) DEFAULT NULL,
  `detail` varchar(300) DEFAULT NULL,
  `group_id` varchar(255) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  `label` varchar(255) DEFAULT NULL,
  `option_id` varchar(255) DEFAULT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `kind` enum('ENVIO_GRATIS','MONTO','PAGO','PARAMETRO') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `exchange` (
  `difference` decimal(12,2) NOT NULL,
  `returned_total` decimal(12,2) NOT NULL,
  `taken_total` decimal(12,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `number` bigint NOT NULL,
  `processed_by_dni` varchar(20) DEFAULT NULL,
  `processed_by_name` varchar(200) DEFAULT NULL,
  `note` varchar(500) DEFAULT NULL,
  `customer_name` varchar(255) NOT NULL,
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `payment_method` enum('CASH','MERCADOPAGO','POSNET','QR_CARD','QR_TRANSFER','TRANSFER') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKp7ls8k7idg6ckvrmn6mafdybn` (`tenant_id`,`number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `exchange_line` (
  `cost_price` decimal(12,2) DEFAULT NULL,
  `idx` int DEFAULT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(12,2) NOT NULL,
  `exchange_id` varchar(255) NOT NULL,
  `id` varchar(255) NOT NULL,
  `product_id` varchar(255) NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `size_value` varchar(255) NOT NULL,
  `kind` enum('DEVUELTA','LLEVADA') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKeh4j46lefcr0d2ci23hg6rmcr` (`exchange_id`),
  CONSTRAINT `FKeh4j46lefcr0d2ci23hg6rmcr` FOREIGN KEY (`exchange_id`) REFERENCES `exchange` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `expense` (
  `amount` decimal(12,2) NOT NULL,
  `expense_date` date NOT NULL,
  `repeat_monthly` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `category_option_id` varchar(255) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  `recurring_group_id` varchar(255) DEFAULT NULL,
  `tenant_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `expense_budget` (
  `monthly_amount` decimal(12,2) NOT NULL,
  `category_option_id` varchar(255) NOT NULL,
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK15uewaphuvfx78q72lr58uqxo` (`tenant_id`,`category_option_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `hero_slide` (
  `position` int NOT NULL,
  `alt` varchar(255) NOT NULL,
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `image_url` mediumtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `marketing_config` (
  `cooldown_days` int NOT NULL,
  `coupon_validity_days` int NOT NULL,
  `daily_email_cap` int NOT NULL,
  `discount_percent` int NOT NULL,
  `enabled` bit(1) NOT NULL,
  `inactivity_days` int NOT NULL,
  `spend_threshold` decimal(12,2) NOT NULL,
  `email_subject` varchar(300) DEFAULT NULL,
  `email_body` varchar(4000) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  `email_image_url` mediumtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `marketing_send` (
  `lifetime_spend_snapshot` decimal(12,2) DEFAULT NULL,
  `last_order_at_snapshot` datetime(6) DEFAULT NULL,
  `sent_at` datetime(6) NOT NULL,
  `coupon_code` varchar(40) DEFAULT NULL,
  `email` varchar(200) NOT NULL,
  `error_message` varchar(500) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `reason` enum('INACTIVE','VIP') NOT NULL,
  `status` enum('FAILED','SENT') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_line` (
  `accepted` bit(1) NOT NULL,
  `cost_estimated` bit(1) NOT NULL,
  `cost_price` decimal(12,2) DEFAULT NULL,
  `idx` int DEFAULT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(12,2) NOT NULL,
  `id` varchar(255) NOT NULL,
  `order_id` varchar(255) NOT NULL,
  `product_id` varchar(255) NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `size_value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7dq8icfu19bqify8imjd0bvvy` (`order_id`),
  CONSTRAINT `FK7dq8icfu19bqify8imjd0bvvy` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orders` (
  `amount_tendered` decimal(12,2) DEFAULT NULL,
  `coupon_discount` decimal(12,2) DEFAULT NULL,
  `discount_amount` decimal(12,2) NOT NULL,
  `discount_percent` int NOT NULL,
  `invoice_punto_venta` int DEFAULT NULL,
  `shipping_lat` double DEFAULT NULL,
  `shipping_lng` double DEFAULT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `invoice_number` bigint DEFAULT NULL,
  `number` bigint NOT NULL,
  `processed_at` datetime(6) DEFAULT NULL,
  `invoice_cae_vencimiento` varchar(10) DEFAULT NULL,
  `confirmed_by_dni` varchar(20) DEFAULT NULL,
  `created_by_dni` varchar(20) DEFAULT NULL,
  `invoice_buyer_cuit` varchar(20) DEFAULT NULL,
  `invoice_cae` varchar(20) DEFAULT NULL,
  `invoice_type` varchar(20) DEFAULT NULL,
  `coupon_code` varchar(40) DEFAULT NULL,
  `mp_payment_id` varchar(100) DEFAULT NULL,
  `mp_preference_id` varchar(100) DEFAULT NULL,
  `confirmed_by_name` varchar(200) DEFAULT NULL,
  `created_by_name` varchar(200) DEFAULT NULL,
  `customer_email` varchar(200) DEFAULT NULL,
  `payment_reference` varchar(200) DEFAULT NULL,
  `free_shipping_note` varchar(300) DEFAULT NULL,
  `discount_note` varchar(500) DEFAULT NULL,
  `invoice_error` varchar(500) DEFAULT NULL,
  `invoice_qr_url` varchar(500) DEFAULT NULL,
  `mp_checkout_url` varchar(500) DEFAULT NULL,
  `payment_issue_note` varchar(500) DEFAULT NULL,
  `shipping_address` varchar(500) DEFAULT NULL,
  `shipping_reference` varchar(500) DEFAULT NULL,
  `customer_name` varchar(255) NOT NULL,
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `channel` enum('LOCAL','WEB') NOT NULL,
  `delivery_method` enum('PICKUP','SHIPPING') NOT NULL,
  `payment_method` enum('CASH','MERCADOPAGO','POSNET','QR_CARD','QR_TRANSFER','TRANSFER') DEFAULT NULL,
  `payment_status` enum('APPROVED','PENDING','REJECTED') DEFAULT NULL,
  `status` enum('CANCELADO','PENDIENTE','PROCESADO') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3cyuqjnp04grve69dyavifhdo` (`tenant_id`,`number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `page_block` (
  `position` int NOT NULL,
  `visible` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `block_type` varchar(40) NOT NULL,
  `page_type` varchar(40) NOT NULL,
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `param_group` (
  `multiple` bit(1) NOT NULL,
  `show_in_catalog` bit(1) NOT NULL,
  `system` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `param_option` (
  `position` int NOT NULL,
  `group_id` varchar(255) NOT NULL,
  `id` varchar(255) NOT NULL,
  `label` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1rbiei4gjpjeq31ax1tro3tao` (`group_id`),
  CONSTRAINT `FK1rbiei4gjpjeq31ax1tro3tao` FOREIGN KEY (`group_id`) REFERENCES `param_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `plan` (
  `max_admin_users` int DEFAULT NULL,
  `max_products` int DEFAULT NULL,
  `show_platform_branding` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `business_model` varchar(20) DEFAULT NULL,
  `slug` varchar(80) NOT NULL,
  `id` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4ievt0jgbe28jjj7na18dw4p3` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `plan_module` (
  `module_key` varchar(40) DEFAULT NULL,
  `plan_id` varchar(255) NOT NULL,
  KEY `FKdoboup97egln7whj5st2pqcje` (`plan_id`),
  CONSTRAINT `FKdoboup97egln7whj5st2pqcje` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `platform_mail_settings` (
  `port` int NOT NULL,
  `from_address` varchar(255) NOT NULL,
  `host` varchar(255) NOT NULL,
  `id` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `product` (
  `active` bit(1) NOT NULL,
  `cost_price` decimal(12,2) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `discontinued` bit(1) NOT NULL,
  `iva_rate` decimal(4,2) DEFAULT NULL,
  `low_stock_threshold` int DEFAULT NULL,
  `price` decimal(12,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `barcode` varchar(64) DEFAULT NULL,
  `video_url` varchar(500) DEFAULT NULL,
  `description` varchar(4000) NOT NULL,
  `age_range` varchar(255) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `size_scale_id` varchar(255) DEFAULT NULL,
  `supplier_id` varchar(255) DEFAULT NULL,
  `tenant_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `product_image` (
  `idx` int NOT NULL,
  `product_id` varchar(255) NOT NULL,
  `url` mediumtext NOT NULL,
  PRIMARY KEY (`idx`,`product_id`),
  KEY `FK404iut26wg4pq85osw3vn0kwd` (`product_id`),
  CONSTRAINT `FK404iut26wg4pq85osw3vn0kwd` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `product_param` (
  `group_id` varchar(255) NOT NULL,
  `option_id` varchar(255) NOT NULL,
  `product_id` varchar(255) NOT NULL,
  PRIMARY KEY (`group_id`,`option_id`,`product_id`),
  KEY `FK5dqywgo4alvwvqhpi3p7xa1sy` (`product_id`),
  CONSTRAINT `FK5dqywgo4alvwvqhpi3p7xa1sy` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `product_size_stock` (
  `idx` int NOT NULL,
  `stock` int NOT NULL,
  `product_id` varchar(255) NOT NULL,
  `size_value` varchar(255) NOT NULL,
  PRIMARY KEY (`idx`,`product_id`),
  KEY `FK2oly5n0ftp2rham006dy96v6w` (`product_id`),
  CONSTRAINT `FK2oly5n0ftp2rham006dy96v6w` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `role` (
  `system` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `name` varchar(60) NOT NULL,
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKjqlra2cn3f4y2tgylsr31jt9s` (`tenant_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `role_permission` (
  `role_id` varchar(255) NOT NULL,
  `permission` enum('CAROUSEL_MANAGE','CASH_REGISTER_VIEW','COUPONS_MANAGE','DISCOUNTS_MANAGE','EXCHANGES_USE','EXPENSES_MANAGE','FINANCE_VIEW','MARKETING_MANAGE','METRICS_VIEW','ORDERS_MANAGE','ORDERS_VIEW','PARAMS_MANAGE','PAYMENTS_MANAGE','PLATFORM_SETTINGS_MANAGE','POS_USE','PRODUCTS_MANAGE','PRODUCTS_VIEW','SHIFTS_MANAGE','SIZE_SCALES_MANAGE','STOCK_MOVEMENTS_VIEW','SUPPLIERS_MANAGE','USERS_MANAGE') DEFAULT NULL,
  KEY `FKp9q4vdsx41116ra1enydewgre` (`role_id`),
  CONSTRAINT `FKp9q4vdsx41116ra1enydewgre` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shift` (
  `closed_at` datetime(6) DEFAULT NULL,
  `opened_at` datetime(6) NOT NULL,
  `user_dni` varchar(20) NOT NULL,
  `user_name` varchar(200) NOT NULL,
  `id` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `site_settings` (
  `arca_enabled` bit(1) NOT NULL,
  `arca_modo_prueba` bit(1) NOT NULL,
  `arca_punto_venta` int DEFAULT NULL,
  `low_stock_alert_enabled` bit(1) NOT NULL,
  `mp_enabled` bit(1) NOT NULL,
  `payment_cash_enabled` bit(1) NOT NULL,
  `payment_qr_card_enabled` bit(1) NOT NULL,
  `payment_qr_transfer_enabled` bit(1) NOT NULL,
  `payment_transfer_enabled` bit(1) NOT NULL,
  `smtp_port` int DEFAULT NULL,
  `arca_cuit` varchar(20) DEFAULT NULL,
  `invoice_mode` varchar(20) NOT NULL,
  `arca_condicion_iva` varchar(40) DEFAULT NULL,
  `cloudinary_cloud_name` varchar(200) DEFAULT NULL,
  `cloudinary_upload_preset` varchar(200) DEFAULT NULL,
  `payment_transfer_alias` varchar(200) DEFAULT NULL,
  `smtp_from_name` varchar(200) DEFAULT NULL,
  `mp_access_token` varchar(300) DEFAULT NULL,
  `mp_public_key` varchar(300) DEFAULT NULL,
  `smtp_from_email` varchar(300) DEFAULT NULL,
  `smtp_host` varchar(300) DEFAULT NULL,
  `smtp_username` varchar(300) DEFAULT NULL,
  `smtp_password` varchar(500) DEFAULT NULL,
  `store_address` varchar(500) DEFAULT NULL,
  `payment_card_link` varchar(1000) DEFAULT NULL,
  `about_text` varchar(2000) DEFAULT NULL,
  `whatsapp_closing` varchar(2000) DEFAULT NULL,
  `whatsapp_intro` varchar(2000) DEFAULT NULL,
  `brand_color` varchar(255) DEFAULT NULL,
  `facebook_url` varchar(255) DEFAULT NULL,
  `footer_color` varchar(255) DEFAULT NULL,
  `header_color` varchar(255) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  `instagram` varchar(255) DEFAULT NULL,
  `layout` varchar(255) DEFAULT NULL,
  `logo_shape` varchar(255) DEFAULT NULL,
  `low_stock_alert_email` varchar(255) DEFAULT NULL,
  `page_background_color` varchar(255) DEFAULT NULL,
  `store_name` varchar(255) NOT NULL,
  `text_color` varchar(255) DEFAULT NULL,
  `theme` varchar(255) DEFAULT NULL,
  `whatsapp_number` varchar(255) NOT NULL,
  `arca_certificado_pem` mediumtext,
  `arca_clave_privada_pem` mediumtext,
  `help_text` mediumtext,
  `faq_text` mediumtext,
  `logo_url` mediumtext,
  `payment_qr_card_image` mediumtext,
  `payment_qr_transfer_image` mediumtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `size_scale` (
  `system` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `size_scale_value` (
  `idx` int NOT NULL,
  `scale_id` varchar(255) NOT NULL,
  `size_value` varchar(255) NOT NULL,
  PRIMARY KEY (`idx`,`scale_id`),
  KEY `FKox34847qhm9ouushrxs9t74` (`scale_id`),
  CONSTRAINT `FKox34847qhm9ouushrxs9t74` FOREIGN KEY (`scale_id`) REFERENCES `size_scale` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `stock_movement` (
  `quantity_delta` int NOT NULL,
  `unit_cost` decimal(12,2) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `note` varchar(500) DEFAULT NULL,
  `created_by_dni` varchar(255) DEFAULT NULL,
  `created_by_name` varchar(255) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  `product_id` varchar(255) NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `reference_id` varchar(255) DEFAULT NULL,
  `size_value` varchar(255) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `reason` enum('AJUSTE_MANUAL','ALTA_INICIAL','CAMBIO_DEVUELTA','CAMBIO_LLEVADA','ENTRADA_COMPRA','VENTA') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `supplier` (
  `created_at` datetime(6) NOT NULL,
  `notes` varchar(2000) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `tenant_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tenant` (
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `slug` varchar(80) NOT NULL,
  `id` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `plan_id` varchar(255) NOT NULL,
  `rubro` enum('FERRETERIA','REPUESTOS','ROPA') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK92y2kiu5uerv9wrjw18xq0b6o` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
