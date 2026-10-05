-- Etapa 1b: el tenant pasa a ser tabla del núcleo, con el sistema que usa,
-- y se suman los dominios propios.

RENAME TABLE `tenant` TO `core_tenant`;

ALTER TABLE `core_tenant`
    ADD COLUMN `sistema` varchar(20) NOT NULL DEFAULT 'ECOMMERCE';

-- Las tiendas con un plan de punto de venta son del sistema POS.
UPDATE `core_tenant` t
    JOIN `plan` p ON p.`id` = t.`plan_id`
SET t.`sistema` = 'POS'
WHERE p.`business_model` = 'POS';

CREATE TABLE `core_dominio` (
    `id`         varchar(255) NOT NULL,
    `tenant_id`  varchar(255) NOT NULL,
    `host`       varchar(255) NOT NULL,
    `principal`  bit(1)       NOT NULL,
    `estado`     varchar(20)  NOT NULL,
    `created_at` datetime(6)  NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_core_dominio_host` (`host`),
    KEY `ix_core_dominio_tenant` (`tenant_id`),
    CONSTRAINT `fk_core_dominio_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `core_tenant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
