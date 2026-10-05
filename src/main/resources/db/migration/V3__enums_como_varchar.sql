-- Regla del proyecto: los enums se guardan como varchar, no como ENUM de MySQL.
-- Con ENUM nativo, sumar un valor (ej: un permiso nuevo) exige alterar la
-- columna; con varchar alcanza con agregarlo al enum de Java.

ALTER TABLE `coupon` MODIFY `kind` varchar(40) NOT NULL;
ALTER TABLE `discount` MODIFY `kind` varchar(40) NOT NULL;
ALTER TABLE `exchange` MODIFY `payment_method` varchar(40) NULL;
ALTER TABLE `exchange_line` MODIFY `kind` varchar(40) NOT NULL;
ALTER TABLE `marketing_send` MODIFY `reason` varchar(40) NOT NULL;
ALTER TABLE `marketing_send` MODIFY `status` varchar(40) NOT NULL;
ALTER TABLE `orders` MODIFY `channel` varchar(40) NOT NULL;
ALTER TABLE `orders` MODIFY `delivery_method` varchar(40) NOT NULL;
ALTER TABLE `orders` MODIFY `payment_method` varchar(40) NULL;
ALTER TABLE `orders` MODIFY `payment_status` varchar(40) NULL;
ALTER TABLE `orders` MODIFY `status` varchar(40) NOT NULL;
ALTER TABLE `role_permission` MODIFY `permission` varchar(40) NULL;
ALTER TABLE `stock_movement` MODIFY `reason` varchar(40) NOT NULL;
ALTER TABLE `core_tenant` MODIFY `rubro` varchar(40) NOT NULL;
