-- Etapa 4a: tabla intermedia producto ↔ grupo de modificadores (quedó afuera de V18 por no tener el prefijo rs_).

create table `rs_producto_grupo_modificador` (`grupo_modificador_id` bigint not null, `producto_id` bigint not null, primary key (`grupo_modificador_id`, `producto_id`)) engine=InnoDB;

alter table `rs_producto_grupo_modificador` add constraint `FKintp12bcjt5b7b7jp8sr9fbke` foreign key (`grupo_modificador_id`) references `rs_grupo_modificador` (`id`);

alter table `rs_producto_grupo_modificador` add constraint `FKmobayj6a0q6r1hytn2a1bdhnt` foreign key (`producto_id`) references `rs_producto` (`id`);
