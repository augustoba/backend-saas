-- Etapa 1c: los planes pasan al núcleo y son de un sistema. Qué sitio público
-- tiene una tienda ya no es un módulo del plan (ECOMMERCE_SITE/POS): lo decide
-- el sistema del tenant.

RENAME TABLE `plan` TO `core_plan`;
RENAME TABLE `plan_module` TO `core_plan_modulo`;

ALTER TABLE `core_plan` ADD COLUMN `sistema` varchar(20) NOT NULL DEFAULT 'ECOMMERCE';
UPDATE `core_plan` SET `sistema` = 'POS' WHERE `business_model` = 'POS';
ALTER TABLE `core_plan` DROP COLUMN `business_model`;

DELETE FROM `core_plan_modulo` WHERE `module_key` IN ('ECOMMERCE_SITE', 'POS');
