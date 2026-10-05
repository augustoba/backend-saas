-- Etapa 2.3: entrega parcial (portada del ecommerce de Ruth). Cada línea del
-- pedido tiene su estado; reemplaza al viejo "aceptada sí/no".
ALTER TABLE `ec_pedido_linea` ADD COLUMN `status` varchar(20) NOT NULL DEFAULT 'PENDIENTE';

-- Pedidos ya resueltos: lo aceptado se entregó, lo destildado no.
UPDATE `ec_pedido_linea` l JOIN `ec_pedido` p ON p.`id` = l.`order_id`
SET l.`status` = CASE
    WHEN p.`status` = 'CANCELADO' THEN 'CANCELADA'
    WHEN p.`status` = 'PROCESADO' AND l.`accepted` THEN 'ENTREGADA'
    WHEN p.`status` = 'PROCESADO' THEN 'CANCELADA'
    ELSE 'PENDIENTE'
END;

ALTER TABLE `ec_pedido_linea` DROP COLUMN `accepted`;
