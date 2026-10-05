-- Etapa 2.2c: la línea del pedido guarda qué variante se vendió (además del
-- nombre como foto del momento). Se completa para los pedidos existentes.
ALTER TABLE `ec_pedido_linea` ADD COLUMN `variante_id` varchar(255) DEFAULT NULL;

UPDATE `ec_pedido_linea` l
    JOIN `ec_variante` v ON v.`product_id` = l.`product_id` AND v.`nombre` = l.`size_value`
SET l.`variante_id` = v.`id`;
