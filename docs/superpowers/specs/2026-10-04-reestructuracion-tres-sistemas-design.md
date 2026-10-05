# Reestructuración: un SaaS, tres sistemas (Ecommerce · POS · Restaurante)

> Spec + registro de avance. Se actualiza al cerrar cada punto (qué se hizo,
> qué falta, commits). Fecha de diseño: 2026-10-04. Rama de trabajo:
> `reestructuracion` en `back/` y en `front/` (main queda intacto hasta el final).

---

## 1. Decisiones tomadas (con el usuario)

| Tema | Decisión |
|---|---|
| Sistemas | **Ecommerce**, **POS** (kiosco/punto de venta) y **Restaurante** |
| Combinación | **Un tenant = un sistema.** No se comparten productos ni stock entre sistemas. Los "combos" se resuelven dentro de cada sistema (ecommerce ya tiene venta en el local; restaurante ya tiene carta online) |
| Despliegue | **Un solo backend Spring Boot + una sola DB MySQL** para todos los tenants |
| Arquitectura | **Núcleo chico compartido + sistemas aislados** (enfoque A). Lo técnico se comparte siempre; lo de negocio se duplica y sólo sube al núcleo cuando un 2º sistema lo necesita **idéntico** (sin `if (sistema == …)`) |
| Estructura de código | Primero **por sistema**, adentro **por capa MVC** (`controller/ service/ repository/ model/ dto/`) |
| Reglas de dependencia | Un sistema nunca importa a otro; `core` nunca importa a un sistema. Lo verifica un test (ArchUnit) |
| Ecommerce de Ruth | **Sigue aparte**, en producción. Es la **referencia funcional** del ecommerce del SaaS: lo que Ruth suma se porta (tabla de paridad, §8) |
| Diseños de tienda | Se mantienen los **35** (15 propios + 20 de Ruth) y la pantalla Apariencia como está |
| Datos | Todo es de prueba: **esquema nuevo desde cero**, sin migración de datos |
| Esquema | **Flyway** reemplaza a `ddl-auto: update` |
| Dominios | Subdominio gratis `slug.<plataforma>` **+ dominios propios** pagados por el cliente |
| Servidor | Un **VPS-1 de OVHcloud** (2 vCores / 4 GB) dedicado al SaaS. La cadetería comparte servidor con Ruth, no con el SaaS |
| Imágenes | Servicio intercambiable en el núcleo: **Cloudinary hoy**, **Cloudflare R2** cuando haya muchas tiendas |

## 2. Estructura

### Backend (`com.saasweb`)
```
core/         controller/ service/ repository/ model/ dto/ config/
ecommerce/    controller/ service/ repository/ model/ dto/
pos/          controller/ service/ repository/ model/ dto/
restaurante/  controller/ service/ repository/ model/ dto/
```
URLs: `/api/core/**`, `/api/ecommerce/**`, `/api/pos/**`, `/api/restaurante/**`
(+ `/api/public/<sistema>/**` para lo que se ve sin login).

### Frontend (Angular 19, una sola app)
```
src/app/core/          login, marco del panel, servicios de tenant/plan/ARCA/imágenes, guards
src/app/shared/        componentes visuales sueltos
src/app/ecommerce/     admin/ + tienda/ (catálogo, carrito, 35 plantillas)
src/app/pos/           admin/ (venta rápida, caja, artículos)
src/app/restaurante/   admin/ + carta/ (menú online público)
```
Cada sistema es una ruta lazy. El panel tiene la marca de la plataforma; la
personalización (colores/diseño) es sólo del sitio público.

## 3. Núcleo (`core`)

| Tabla | Contenido |
|---|---|
| `core_tenant` | slug, nombre, **sistema** (`ECOMMERCE`/`POS`/`RESTAURANTE`), rubro, plan, activo |
| `core_dominio` | tenant, host, principal, estado (`PENDIENTE`/`ACTIVO`) |
| `core_plan`, `core_plan_modulo` | planes por sistema y sus módulos |
| `core_usuario`, `core_rol`, `core_rol_permiso` | login por DNI; permisos = strings que declara cada sistema |
| `core_identidad` | nombre comercial, logo, forma del logo, color de marca, WhatsApp, Instagram, Facebook, dirección, foto del local, texto "nosotros" |
| `core_config_arca` | CUIT, punto de venta, condición IVA, certificado, clave, modo prueba, modo de comprobante |
| `core_config_mp` | access token, public key, habilitado |
| `core_config_mail` | SMTP propio de la tienda |
| `core_config_imagenes` | proveedor, cloud name, preset, API key/secret |
| `core_comprobante` | factura/NC: tipo, CAE, vencimiento, número, PV, QR, error, CUIT comprador, total, **origen** (`sistema` + `id`) |
| `core_cobro_mp` | preference id, checkout url, payment id, estado, nota, **origen** |
| `core_mail_plataforma` | SMTP del operador |

**Origen** = dos strings (`"ECOMMERCE"`, `"<id>"`) sin FK: el núcleo factura y
cobra sin conocer a los sistemas. Cuando cambia el estado de un cobro, el
núcleo publica un evento (`CobroActualizado`) que el sistema dueño escucha.

### Resolución de tenant (por `Host`)
1. `core_dominio` por host exacto (dominios propios, estado ACTIVO).
2. Si termina en `.<dominio-plataforma>`, el prefijo es el slug.
3. Si no, 404 "tienda no encontrada". `X-Demo-Tenant` sólo en desarrollo.

Links de mails y `back_urls` de Mercado Pago se arman con el **dominio
principal** del tenant.

### SSL de dominios propios
Caddy con *on-demand TLS*: antes de emitir, consulta
`GET /api/public/core/dominios/permitido?host=…` (200 sólo si el host es de un
tenant activo).

## 4. Ecommerce (`ec_`)

Base funcional: **lo último de Ruth** + lo propio del SaaS (multi-tenant,
ARCA, Apariencia/colores, bloques de página, 35 diseños).

| Tabla | Nota |
|---|---|
| `ec_producto` | nombre, descripción, precio, costo, IVA, código de barras, umbral stock, activo, discontinuado, eliminado, video, proveedor, **mostrar_en_promos** |
| `ec_producto_imagen` | galería ordenada (1ª = portada) |
| `ec_variante` | producto, nombre ("M", "Rojo 42"), stock, código de barras — **reemplaza a talle/SizeStock** |
| `ec_escala_variante` (+ valores) | plantillas de variantes ("Talles bebé") |
| `ec_param_grupo`, `ec_param_opcion`, `ec_producto_param` | parametrías (la "edad" pasa a ser una) |
| `ec_pedido`, `ec_pedido_linea` | canal WEB/LOCAL; línea con **estado** PENDIENTE/ENTREGADA/CANCELADA, apunta a variante, congela nombre y costo |
| `ec_cambio`, `ec_cambio_linea` | admite **devolución pura** |
| `ec_movimiento_stock` | con costo |
| `ec_descuento`, `ec_cupon` | |
| `ec_config_tienda` | diseño, theme, colores, textos WhatsApp, medios de pago manuales, ayuda/FAQ, alerta de stock, **banner promo**, **página nosotros activa** |
| `ec_carrusel`, `ec_bloque_pagina`, `ec_marketing_config`, `ec_marketing_envio` | |
| `ec_turno`, `ec_proveedor`, `ec_gasto`, `ec_presupuesto_gasto` | |

## 5. POS (`pos_`)

| Tabla | Nota |
|---|---|
| `pos_articulo` | nombre, código de barras, precio, costo, IVA, stock, umbral, categoría |
| `pos_categoria` | |
| `pos_venta`, `pos_venta_linea` | ticket: medio de pago, monto entregado, cajero |
| `pos_caja`, `pos_movimiento_caja` | apertura/cierre con arqueo (la caja es el turno) |
| `pos_movimiento_stock`, `pos_proveedor`, `pos_gasto` | |

Sin sitio público: `/` lleva al login.

## 6. Restaurante (`rs_`)

Se porta el proyecto `C:\proyectos\restaurante` (backend + frontend) quitándole
su tenant/usuario/rol (pasan a ser los del núcleo). Tablas: `rs_sucursal`,
`rs_sector`, `rs_salon`, `rs_mesa`, `rs_categoria`, `rs_producto`,
`rs_grupo_modificador`, `rs_modificador`, `rs_lista_precio(_item)`,
`rs_orden`, `rs_orden_item`, `rs_pago`, `rs_solicitud_mesa`, `rs_caja`,
`rs_arqueo`, `rs_movimiento_caja`, `rs_turno`, `rs_cliente`,
`rs_cuenta_corriente_mov`, `rs_reserva`, `rs_turno_reserva`,
`rs_config_reserva_salon`, `rs_proveedor`, `rs_gasto`, `rs_categoria_gasto`,
`rs_clasificacion_gasto`, `rs_movimiento_stock`, `rs_menu_online_config`,
`rs_comentario_producto`, `rs_zona_envio`, `rs_medio_pago`, `rs_preferencias`.

Sitio público: carta online + pedido desde mesa (QR) + reservas.

## 7. Capacidad y costos (estimado, a confirmar con prueba de carga en Etapa 5)

- VPS-1 OVH (2 vCores/4 GB, ~US$ 4,5/mes): SO+Caddy ~0,4 GB, MySQL ~1 GB,
  backend ~1 GB → **15–20 tiendas cómodas, techo estimado 30–50**.
- Servidor ≈ US$ 0,25/cliente/mes con 20 clientes.
- Imágenes: el gratis de Cloudinary (~25 créditos) no alcanza para tráfico de
  ~20 tiendas → pasar a R2 (tráfico gratis) cambiando sólo el servicio del núcleo.

## 8. Paridad Ecommerce de Ruth → SaaS

Se actualiza cada vez que Ruth suma algo. El ecommerce del SaaS está "al día"
cuando no hay filas en ⏳.

| Función de Ruth | Estado |
|---|---|
| 21 diseños de tienda (20 + Clásico) con vocabulario de tienda | ✅ (Fase 19) |
| Entrega/cancelación parcial de pedidos (estado por línea, agregar ítem) | ✅ (2.3a) |
| Devolución pura en Cambios | ⏳ |
| Ajuste masivo de precio | ⏳ |
| Eliminar producto definitivo (+ borrar fotos en el proveedor) | ⏳ |
| "Mostrar en promos" + vista `/promos` con fallback a descuentos | ⏳ |
| Banner promocional emergente | ⏳ |
| Página "Nosotros" + foto del local | ⏳ |
| WhatsApp flotante | ⏳ |
| "Coordinar por WhatsApp" tras pago aprobado | ⏳ |
| Título de pestaña "Página \| Tienda" | ⏳ |
| Medios de pago del local POSNET/Efectivo/Transferencia | ⏳ |
| Auditoría de over-fetching del catálogo público | ⏳ |

## 9. Etapas y avance

Cada etapa: compila, tests en verde, prueba en navegador, commit, y se
actualiza esta sección.

### Etapa 0 — Preparación ✅ (2026-10-04)
- [x] Rama `reestructuracion` en back y front
- [x] Flyway en lugar de `ddl-auto: update`. **V1** = esquema previo exacto
      (generado por Hibernate en una base vacía + `mysqldump --no-data`).
      Hibernate pasa a `validate`. La rama usa la base **`saasweb_v2`**
      (`DB_URL` la sobreescribe); `main` sigue con `saasweb`. Tests: H2 con
      Flyway apagado (los scripts son SQL de MySQL).
- [x] Paquetes `ecommerce/ pos/ restaurante/` (con `package-info.java`) y
      `ArquitecturaTest` (ArchUnit). Verificado que detecta una violación real
      (clase en `pos` usando `ecommerce` → falla).
- [x] Front: `src/app/{ecommerce,pos,restaurante}/` + `scripts/check-arquitectura.mjs`
      en `prebuild` (sin ESLint, sin dependencias). Verificado con una violación
      de prueba.
- Verificación: `mvn test` 37/37 (35 previos + 2 de arquitectura); el backend
  arranca contra MySQL, Flyway aplica V1, Hibernate valida y la API responde
  con el seed; `npm run build` pasa el chequeo y compila.
- Nota: los paquetes del núcleo hoy siguen siendo `core/<feature>/` (por
  funcionalidad). Se reordenan por capa en la Etapa 1, a medida que cada
  parte se reescribe; el código del ecommerce que hoy vive en `core/` se muda
  en la Etapa 2.

### Etapa 1 — Núcleo ✅ (2026-10-04)
- [x] **1a** Núcleo reordenado por capa: `core/{controller,service,repository,model,dto}`
      con tenant, usuarios/roles, auth, planes, settings, ARCA, Mercado Pago y mail de
      plataforma; `config/` y `common/` pasan a `core/`. Movimiento mecánico, sin lógica nueva.
- [x] **1b** `core_tenant` con `sistema` + `core_dominio` + resolución por Host
      (`TenantResolver`: dominio propio ACTIVO → `<slug>.<plataforma>` → raíz con
      `X-Demo-Tenant` → 404). ABM en `/api/core/dominios` (permiso nuevo
      `DOMAINS_MANAGE`), verificación de DNS contra `app.platform.server-ip`,
      consulta de Caddy en `/api/public/core/dominios/permitido`. Links de
      recuperar contraseña y vuelta de Mercado Pago con `DominioService.urlPublica`.
      En desarrollo la plataforma es `localhost`: `estilos-pequenos.localhost` anda.
      **V3**: todas las columnas `ENUM` de MySQL pasan a `varchar` (regla del
      proyecto: agregar un valor a un enum de Java no toca la base).
- [x] **1c** Planes y permisos por sistema:
      - `core_plan` / `core_plan_modulo` (V4) con `sistema`; planes sembrados
        "Ecommerce", "Punto de venta" y "Restaurante"; `PlanService.defaultFor(sistema)`.
        Los módulos `ECOMMERCE_SITE` y `POS` desaparecen: el sitio público lo decide
        el sistema (POS = sin sitio, `sin_sitio_online`).
      - Alta de tienda con `sistema` (vacío = el del plan); plan de otro sistema → 400.
        La tienda nace con sus roles por defecto (antes las tiendas del asistente
        nacían **sin roles**).
      - Permisos: el enum `Permission` se reemplaza por catálogos
        (`CatalogoPermisos`): `PermisosNucleo` en el núcleo y `PermisosEcommerce`,
        `PermisosPos`, `PermisosRestaurante` (vacío hasta la Etapa 4) en cada
        sistema. `PermisoService` arma los asignables y los roles por defecto
        ("Administrador" + "Vendedor"/"Cajero"). Los permisos reservados al
        superadmin (`PLATFORM_SETTINGS_MANAGE`, `CAROUSEL_MANAGE`) ya **no se pueden
        asignar** desde el ABM de roles (antes sí).
      - `/api/settings` y `/api/admin/tenants` devuelven `sistema` (se mantienen
        `posEnabled`/`ecommerceSiteEnabled`/`businessModel` derivados para el front actual).
- [x] **1d** Admin inicial: el alta de tienda acepta un bloque opcional `admin`
      (nombre, apellido, DNI, email, contraseña) y crea ese usuario con el rol
      "Administrador" de la tienda. Entra por el dominio de SU tienda; el mismo DNI
      no existe en otra. (Falta el paso en el asistente del front: va con el panel.)
- [x] **1e** Identidad y configuraciones del núcleo (V5): `core_identidad`,
      `core_config_arca`, `core_config_mp`, `core_config_mail`,
      `core_config_imagenes` (una fila por tienda, datos copiados desde
      `site_settings` y columnas borradas de ahí). `ConfiguracionService` las
      maneja; ARCA, Mercado Pago, mails y PDFs leen de ahí. En `site_settings`
      queda sólo lo de la tienda online (diseño, colores, medios de pago manuales,
      textos, alerta de stock): pasa a `ec_config_tienda` en la Etapa 2.
      Verificado: las 7 respuestas de settings (pública, MP, ARCA, Cloudinary,
      mail, alertas, y la de otra tienda) quedaron **byte por byte idénticas**
      antes y después de la migración; editar la identidad escribe en
      `core_identidad` y los textos de la tienda en `site_settings`.
- [x] **1f** `core_comprobante` (V6) con origen (sistema + id, sin FK) y clase
      FACTURA / NOTA_CREDITO; `ComprobanteService.registrar/deOrigen`. Las notas
      de crédito ya viven ahí (`credit_note` migrada y borrada; la API
      `/api/admin/orders/{id}/credit-notes` responde igual).
      **Pasan a la Etapa 2 (a propósito):** sacar del pedido los campos `invoice*`
      y `mp*` y el cobro de Mercado Pago (`core_cobro_mp` + evento). El pedido se
      rehace como `ec_pedido` en esa etapa; hacerlo ahora sería migrarlo dos veces.
- [x] **1g** Front del núcleo (front `3e4f873`): `sistema` en settings/tiendas/planes;
      menú del panel filtrado por sistema (arregla que ARCA sólo se veía en POS);
      pantalla `/admin/config/dominios`; asistente "Crear tienda" con selector de
      sistema (filtra planes) y administrador inicial. Proxy de `ng serve` con
      `changeOrigin: false` para probar subdominios (`<slug>.localhost:4200`).
- [x] Verificado (Playwright headless con el Chrome instalado, sin errores de
      consola): superadmin ve dominios, tiendas con su sistema y el asistente
      filtra planes por sistema; el admin de una tienda **restaurante** entra por
      `parrilla-prueba.localhost:4200` y ve sólo lo del núcleo; el admin del
      ecommerce piloto ve su menú completo.
- [x] **1h** Servicio de imágenes intercambiable: `ProveedorImagenes` (hoy
      `CloudinaryImagenes`; R2 = otra implementación con PUT prefirmado),
      `core_config_imagenes.proveedor` (V7) y `POST /api/core/imagenes/subidas`
      que devuelve el ticket. El front (`ImagenService`, front `70bbb68`) sube según
      el ticket, sin saber el proveedor. **Arregla** que todas las tiendas subían a
      la carpeta `estilos-pequenos/…`: ahora la carpeta siempre va debajo del slug
      de la tienda (sólo el superadmin puede apuntar a otra, para el logo del
      asistente). Verificado: el restaurante de prueba recibe `parrilla-prueba/carta`
      y la piloto `estilos-pequenos/productos`. No se subió una foto real (la cuenta
      configurada es la de producción de la piloto).
- [ ] Pendiente para las etapas 3/4: el asistente sigue mostrando el paso de
      diseño de tienda online aunque el sistema sea POS o restaurante.

### Etapa 2 — Ecommerce ⏳ (en curso)
- [x] **2.1** Código del ecommerce mudado a `ecommerce/{controller,service,repository,model,dto}`
      (117 clases: los 14 paquetes `core/<feature>` + `modules/ropa` + settings de
      tienda, notas de crédito, PDF/mail de factura y webhook de Mercado Pago).
      Dependencias del núcleo hacia el ecommerce invertidas (ArchUnit en verde):
      - Alta: `TenantProvisioningService` (núcleo: tienda, roles, admin, identidad)
        + `InicializadorDeSistema` → `InicializadorEcommerce` (settings, home,
        carrusel, gastos y productos de ejemplo; atiende también POS hasta la Etapa 3).
      - Baja: `LimpiezaDeTienda` → `LimpiezaEcommerce`.
      - Arranque: `DataSeeder` del núcleo (`@Order(1)`) + `SeedEcommerce` (`@Order(2)`).
      - Configuración: `ConfiguracionDtos` + `ConfiguracionController` en el núcleo
        (mismas URLs `/api/admin/settings/{mercadopago,arca,cloudinary,mail}`);
        `LAYOUT_PATTERN` pasa a `TenantAdminDtos`.
      Sin cambios de esquema ni de API. Verificado: 51/51 tests; las 6 respuestas
      de settings idénticas a las de antes; 13 endpoints públicos y del panel en
      200; alta de una tienda ferretería (4 productos de ejemplo, colores del
      asistente aplicados) y su baja definitiva (no queda ninguna fila).
- [x] **2.2a** Tablas con prefijo (V8 ecommerce, V9 núcleo), con los nombres del
      spec: `ec_producto`, `ec_pedido`, `ec_pedido_linea`, `ec_cambio`, `ec_cupon`,
      `ec_descuento`, `ec_config_tienda`, `ec_escala_talle`, etc.;
      `core_usuario`, `core_rol`, `core_rol_permiso`, `core_mail_plataforma`. Sólo
      cambia el nombre de la tabla. Verificado: 13 respuestas de la API (catálogo,
      settings, roles, usuarios, gastos, proveedores, cupones, bloques, movimientos
      de stock) idénticas antes y después; la base ya no tiene tablas sin prefijo.
- [x] **2.2b** Talle → variante (V10): `ec_variante` con id propio, nombre, stock,
      código de barras opcional y posición (las 62 filas de talle migradas, cada
      una con su id); escalas → `ec_escala_variante(_valor)`. `Product` tiene las
      variantes como entidades (`@OneToMany`) y guardar el producto **fusiona por
      nombre** (`reemplazarVariantes`): las existentes conservan id y código de
      barras. API sin cambios (`sizeStocks[].size/stock`); `Variante.getSize()` es
      un alias que se va con 2.2c.
      Verificado contra MySQL: 13 respuestas idénticas; pedido confirmado descuenta
      el stock de la variante; editar el producto conserva el código de barras.
      VarianteTest. 52/52.
- [x] **2.2c** Código de barras por variante (front `1768d9b`):
      - API: cada variante trae `id` y `barcode`; al guardar se valida que el
        código sea único en la tienda (entre productos y variantes) y no esté
        repetido; `by-barcode` también encuentra variantes.
      - V11: `ec_pedido_linea.variante_id` (la línea guarda qué variante se vendió;
        el nombre queda como foto). Completado para los pedidos existentes.
      - Front: campo de código en cada variante del formulario; kiosco y venta en
        el local suman **la variante escaneada** (`core/utils/barcode-match.ts`).
      - **Arreglado** (bug latente): el seed del ecommerce recorría pedidos fuera
        de una transacción y el arranque fallaba en cuanto había un pedido
        procesado; `SeedEcommerce.run` ahora es `@Transactional`.
      - Verificado: 53/53; E2E con Playwright (cargar código en una variante →
        escanearlo en "Venta en el local" suma esa variante), sin errores de consola.
      - **Decisión del usuario:** "Edad" (`ageRange`) queda como campo opcional del
        producto (no pasa a parametría): las 35 plantillas la muestran y otros
        rubros simplemente la dejan vacía.
      - Queda para más adelante: renombrar "talle"/`size` a "variante" en la API y
        en las pantallas (hoy el alias `Variante.getSize()` mantiene compatibilidad).
- [x] **2.3a** Entrega y cancelación parcial de pedidos (de Ruth):
      - V12: `ec_pedido_linea.status` PENDIENTE/ENTREGADA/CANCELADA reemplaza a
        `accepted` (backfill de pedidos existentes).
      - Endpoints `confirm-lines`, `cancel-lines`, agregar/cambiar cantidad/sacar
        línea; confirmar o cancelar el pedido entero = atajo sobre lo pendiente.
        El pedido se resuelve (PROCESADO/CANCELADO) cuando no queda nada
        pendiente; lo cancelado deja de cobrarse; la venta en el local se factura
        con ARCA al resolverse, por lo entregado. Se mantiene el aviso de pago de
        Mercado Pago aprobado que no se pudo confirmar (propio del SaaS).
      - Front: detalle del pedido de Ruth + notas de crédito y reintento de
        factura del SaaS.
      - Verificado: EntregaParcialTest (2 casos), 55/55; E2E en el navegador
        (entregar 1 de 2 → sigue pendiente; cancelar el otro → PROCESADO cobrando
        sólo lo entregado), sin errores de consola.
- [ ] **2.3b** Factura y cobro de Mercado Pago del pedido vía el núcleo
      (`core_comprobante` para las facturas, `core_cobro_mp` + evento)
- [ ] Facturación y cobro vía núcleo
- [ ] Paridad con Ruth (§8) sin pendientes
- [ ] 35 diseños + Apariencia funcionando
- [ ] Verificado: compra WhatsApp y MP, venta en local, cambio, factura ARCA de prueba

### Etapa 3 — POS ⏳
- [ ] Artículos, venta rápida con lector, caja con arqueo, stock, gastos
- [ ] Verificado: abrir caja, vender escaneando, facturar, cerrar con arqueo

### Etapa 4 — Restaurante ⏳
- [ ] Portar back + front sobre el núcleo, tablas `rs_`
- [ ] Verificado: comanda en mesa → KDS → cobro → factura; carta online y reservas

### Etapa 5 — Despliegue ⏳
- [ ] VPS OVH + Caddy (on-demand TLS) + backups MySQL
- [ ] Dominio de plataforma + un dominio propio de prueba
- [ ] Prueba de carga (~20 tiendas simuladas)

## 10. Registro de avance

| Fecha | Qué | Commits |
|---|---|---|
| 2026-10-04 | Fase 19: 20 diseños de Ruth en el SaaS (previo a la reestructuración) | back `a675938`, front `ce97e14` |
| 2026-10-04 | Spec escrito | back `672cbb7` |
| 2026-10-04 | Etapa 0: Flyway (V1), paquetes por sistema, ArchUnit, chequeo de imports del front | back `c375799`, front `958168b` |
| 2026-10-04 | 1a: núcleo reordenado por capa | back `7b41fa7` |
| 2026-10-04 | 1b: sistema del tenant, dominios propios, resolución por Host, enums como varchar | back `5786850` |
| 2026-10-04 | 1c: planes y permisos por sistema, roles por defecto al dar de alta | back `8259da5` |
| 2026-10-04 | 1d: admin inicial en el alta de tienda | back `ab9d9d9` |
| 2026-10-04 | 1e: identidad y configuraciones del núcleo fuera de site_settings | back `a76b6fa` |
| 2026-10-04 | 1f: comprobantes en el núcleo; notas de crédito migradas | back `933616e` |
| 2026-10-04 | 1g: panel según el sistema, dominios, asistente con sistema + admin | front `3e4f873` |
| 2026-10-04 | 1h: servicio de imágenes intercambiable (Etapa 1 cerrada) | back `ace29d1`, front `70bbb68` |
| 2026-10-05 | 2.1: ecommerce mudado a su paquete; alta, baja, seed y configuración invertidos | back `f3689f5` |
| 2026-10-05 | 2.2a: todas las tablas con prefijo ec_/core_ (V8, V9) | back `03eea7e` |
| 2026-10-05 | 2.2b: talle a variante con identidad propia (V10) | back `93f817c` |
| 2026-10-05 | 2.2c: código de barras por variante (V11), seed transaccional | back `70c344d`, front `1768d9b` |
| 2026-10-05 | 2.3a: entrega y cancelación parcial de pedidos (V12) | back (ver git log), front `696efb7` |
