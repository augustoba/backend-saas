-- Etapa 2.2a: las últimas tablas del núcleo sin prefijo pasan a core_ (ver el spec).
RENAME TABLE
    `admin_user` TO `core_usuario`,
    `role` TO `core_rol`,
    `role_permission` TO `core_rol_permiso`,
    `platform_mail_settings` TO `core_mail_plataforma`;
