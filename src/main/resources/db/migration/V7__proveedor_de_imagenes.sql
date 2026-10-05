-- Etapa 1h: el proveedor de imágenes es intercambiable por tienda (hoy todas
-- usan Cloudinary; el spec prevé Cloudflare R2).
ALTER TABLE `core_config_imagenes` ADD COLUMN `proveedor` varchar(20) NOT NULL DEFAULT 'CLOUDINARY';
