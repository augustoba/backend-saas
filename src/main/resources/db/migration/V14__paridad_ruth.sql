-- Etapa 2.4: lo nuevo del ecommerce de Ruth.
ALTER TABLE `ec_producto` ADD COLUMN `featured_in_promos` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `ec_config_tienda`
  ADD COLUMN `about_page_enabled` bit(1) NOT NULL DEFAULT b'0',
  ADD COLUMN `promo_banner_enabled` bit(1) NOT NULL DEFAULT b'0',
  ADD COLUMN `promo_banner_image` mediumtext,
  ADD COLUMN `promo_banner_link` varchar(500) DEFAULT NULL;

ALTER TABLE `core_identidad` ADD COLUMN `store_photo_url` mediumtext;

-- Credenciales firmadas de Cloudinary: sólo para borrar fotos (borrado definitivo de productos).
ALTER TABLE `core_config_imagenes`
  ADD COLUMN `cloudinary_api_key` varchar(100) DEFAULT NULL,
  ADD COLUMN `cloudinary_api_secret` varchar(200) DEFAULT NULL;
