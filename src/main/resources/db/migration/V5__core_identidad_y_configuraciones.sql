-- Etapa 1e: la identidad del comercio y sus credenciales (ARCA, Mercado Pago,
-- mail, imágenes) salen de site_settings a tablas del núcleo, una fila por
-- tienda (id = tenant). En site_settings queda sólo lo de la tienda online.

CREATE TABLE `core_identidad` (
  `id` varchar(255) NOT NULL,
  `store_name` varchar(255) NOT NULL,
  `whatsapp_number` varchar(255) NOT NULL,
  `about_text` varchar(2000) DEFAULT NULL,
  `instagram` varchar(255) DEFAULT NULL,
  `facebook_url` varchar(255) DEFAULT NULL,
  `logo_url` mediumtext,
  `logo_shape` varchar(255) DEFAULT NULL,
  `brand_color` varchar(255) DEFAULT NULL,
  `store_address` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `core_config_arca` (
  `id` varchar(255) NOT NULL,
  `arca_enabled` bit(1) NOT NULL,
  `arca_modo_prueba` bit(1) NOT NULL,
  `arca_cuit` varchar(20) DEFAULT NULL,
  `arca_punto_venta` int DEFAULT NULL,
  `arca_condicion_iva` varchar(40) DEFAULT NULL,
  `arca_certificado_pem` mediumtext,
  `arca_clave_privada_pem` mediumtext,
  `invoice_mode` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `core_config_mp` (
  `id` varchar(255) NOT NULL,
  `mp_enabled` bit(1) NOT NULL,
  `mp_access_token` varchar(300) DEFAULT NULL,
  `mp_public_key` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `core_config_mail` (
  `id` varchar(255) NOT NULL,
  `smtp_host` varchar(300) DEFAULT NULL,
  `smtp_port` int DEFAULT NULL,
  `smtp_username` varchar(300) DEFAULT NULL,
  `smtp_password` varchar(500) DEFAULT NULL,
  `smtp_from_email` varchar(300) DEFAULT NULL,
  `smtp_from_name` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `core_config_imagenes` (
  `id` varchar(255) NOT NULL,
  `cloudinary_cloud_name` varchar(200) DEFAULT NULL,
  `cloudinary_upload_preset` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `core_identidad` (`id`, `store_name`, `whatsapp_number`, `about_text`, `instagram`, `facebook_url`,
                              `logo_url`, `logo_shape`, `brand_color`, `store_address`)
SELECT `id`, `store_name`, `whatsapp_number`, `about_text`, `instagram`, `facebook_url`,
       `logo_url`, `logo_shape`, `brand_color`, `store_address`
FROM `site_settings`;

INSERT INTO `core_config_arca` (`id`, `arca_enabled`, `arca_modo_prueba`, `arca_cuit`, `arca_punto_venta`,
                                `arca_condicion_iva`, `arca_certificado_pem`, `arca_clave_privada_pem`, `invoice_mode`)
SELECT `id`, `arca_enabled`, `arca_modo_prueba`, `arca_cuit`, `arca_punto_venta`,
       `arca_condicion_iva`, `arca_certificado_pem`, `arca_clave_privada_pem`, `invoice_mode`
FROM `site_settings`;

INSERT INTO `core_config_mp` (`id`, `mp_enabled`, `mp_access_token`, `mp_public_key`)
SELECT `id`, `mp_enabled`, `mp_access_token`, `mp_public_key` FROM `site_settings`;

INSERT INTO `core_config_mail` (`id`, `smtp_host`, `smtp_port`, `smtp_username`, `smtp_password`,
                                `smtp_from_email`, `smtp_from_name`)
SELECT `id`, `smtp_host`, `smtp_port`, `smtp_username`, `smtp_password`, `smtp_from_email`, `smtp_from_name`
FROM `site_settings`;

INSERT INTO `core_config_imagenes` (`id`, `cloudinary_cloud_name`, `cloudinary_upload_preset`)
SELECT `id`, `cloudinary_cloud_name`, `cloudinary_upload_preset` FROM `site_settings`;

ALTER TABLE `site_settings`
  DROP COLUMN `store_name`, DROP COLUMN `whatsapp_number`, DROP COLUMN `about_text`,
  DROP COLUMN `instagram`, DROP COLUMN `facebook_url`, DROP COLUMN `logo_url`,
  DROP COLUMN `logo_shape`, DROP COLUMN `brand_color`, DROP COLUMN `store_address`,
  DROP COLUMN `arca_enabled`, DROP COLUMN `arca_modo_prueba`, DROP COLUMN `arca_cuit`,
  DROP COLUMN `arca_punto_venta`, DROP COLUMN `arca_condicion_iva`, DROP COLUMN `arca_certificado_pem`,
  DROP COLUMN `arca_clave_privada_pem`, DROP COLUMN `invoice_mode`,
  DROP COLUMN `mp_enabled`, DROP COLUMN `mp_access_token`, DROP COLUMN `mp_public_key`,
  DROP COLUMN `smtp_host`, DROP COLUMN `smtp_port`, DROP COLUMN `smtp_username`,
  DROP COLUMN `smtp_password`, DROP COLUMN `smtp_from_email`, DROP COLUMN `smtp_from_name`,
  DROP COLUMN `cloudinary_cloud_name`, DROP COLUMN `cloudinary_upload_preset`;
