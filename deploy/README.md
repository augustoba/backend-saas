# Despliegue del SaaS (VPS-1 de OVHcloud)

Un servidor para todas las tiendas: **Caddy** (HTTPS automático, también para los
dominios propios) → **backend** (jar + systemd, sólo escucha en 127.0.0.1) →
**MySQL** (misma máquina). El front (Angular compilado) lo sirve Caddy.

| Archivo | Va en el servidor como |
|---|---|
| `Caddyfile` | `/etc/caddy/Caddyfile` |
| `saasweb.service` | `/etc/systemd/system/saasweb.service` |
| `saasweb.env.example` | `/etc/saasweb/saasweb.env` (completado, `chmod 640`) |
| `backup-mysql.sh` | `/srv/saasweb/backup-mysql.sh` + cron |
| `publicar.sh` | se corre desde tu máquina para cada versión |
| `prueba-carga.mjs` | se corre desde tu máquina |

## 1. Servidor (una sola vez)

Ubuntu 24.04 en el VPS-1 (2 vCores / 4 GB). Conectado por SSH como root:

```bash
apt update && apt upgrade -y
apt install -y openjdk-21-jre-headless mysql-server curl
# Caddy (repositorio oficial)
apt install -y debian-keyring debian-archive-keyring apt-transport-https
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' > /etc/apt/sources.list.d/caddy-stable.list
apt update && apt install -y caddy

# Firewall: sólo SSH y web. El 8080 y el 3306 quedan cerrados (escuchan en 127.0.0.1).
ufw allow OpenSSH && ufw allow 80 && ufw allow 443 && ufw enable

# Usuario del servicio y carpetas
useradd --system --home /srv/saasweb --shell /usr/sbin/nologin saasweb
mkdir -p /srv/saasweb /etc/saasweb /var/log/caddy
```

**MySQL** (usuario dedicado, no root):

```sql
CREATE DATABASE saasweb CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'saasweb'@'127.0.0.1' IDENTIFIED BY 'UNA_CLAVE_LARGA';
GRANT ALL PRIVILEGES ON saasweb.* TO 'saasweb'@'127.0.0.1';
```

El esquema lo crea Flyway en el primer arranque (V1…V19); no hay que correr SQL a mano.

**Variables**: copiar `saasweb.env.example` a `/etc/saasweb/saasweb.env`, completar
todo lo que dice `CAMBIAR` (claves con `openssl rand -base64 48`), y:
`chown root:saasweb /etc/saasweb/saasweb.env && chmod 640 /etc/saasweb/saasweb.env`.

**Servicio y Caddy**: copiar `saasweb.service` y el `Caddyfile` (con tu mail), y
`systemctl daemon-reload && systemctl enable saasweb && systemctl reload caddy`.

## 2. Dominio de la plataforma

En el registrador del dominio (ej. `miplataforma.com`), registros **A**:

| Nombre | Apunta a |
|---|---|
| `@` (raíz) | IP del VPS |
| `*` (comodín: todas las tiendas `<slug>.miplataforma.com`) | IP del VPS |

Con eso, cada tienda nueva funciona sola, sin tocar DNS: Caddy pide su
certificado la primera vez que alguien entra (el backend confirma que la tienda existe).

**Dominio propio de una tienda** (lo paga el cliente): en su registrador, un registro
**A** de `www.sutienda.com` (y/o la raíz) a la IP del VPS; después se carga en el
panel en *Configuración → Dominios*. Se activa cuando el DNS ya apunta al servidor.

## 3. Publicar una versión

Desde tu máquina, en Git Bash, parado en `saas/back`:

```bash
./deploy/publicar.sh root@IP_DEL_VPS
```

Compila backend (con tests) y front, sube, guarda la versión anterior
(`backend.jar.anterior`, `front.anterior`) y reinicia. Volver atrás:
`cp backend.jar.anterior backend.jar && systemctl restart saasweb` (ojo: si la versión
nueva traía una migración, la base ya quedó migrada; las migraciones del proyecto
sólo agregan, así que la versión anterior en general sigue andando).

## 4. Backups

```bash
cp backup-mysql.sh /srv/saasweb/ && chmod 700 /srv/saasweb/backup-mysql.sh
printf "[mysqldump]\nuser=saasweb\npassword=UNA_CLAVE_LARGA\n" > /root/.my.cnf && chmod 600 /root/.my.cnf
crontab -e   # agregar:  15 4 * * *  /srv/saasweb/backup-mysql.sh >> /var/log/saasweb-backup.log 2>&1
```

Guarda 14 días en el servidor. **Copia afuera** (recomendado): instalar `rclone`,
configurar un remoto llamado `backup` (OVH Object Storage o Cloudflare R2) y el
script sube cada backup solo. Probar una restauración de vez en cuando:
`gunzip < archivo.sql.gz | mysql saasweb_prueba`.

## 5. Checklist antes de dar el deploy por terminado

- [ ] `https://miplataforma.com` abre la tienda piloto con candado.
- [ ] Una tienda de prueba nueva abre en `https://<slug>.miplataforma.com` sin tocar DNS.
- [ ] `SEED_ENABLED=false` y `TENANT_DEMO_SWITCH_ENABLED=false` (sin datos de ejemplo
      ni selector de tienda demo).
- [ ] Login de superadmin y de la tienda piloto con las claves reales.
- [ ] Desde afuera, `http://IP:8080` y `IP:3306` no responden (firewall).
- [ ] Mail de recuperación de contraseña llega (SMTP real).
- [ ] Webhook de Mercado Pago: `BACKEND_PUBLIC_URL` es la URL pública real.
- [ ] Corrió un backup y se pudo restaurar en una base de prueba.

## Capacidad (prueba de carga)

`node deploy/prueba-carga.mjs <url> <usuarios> <segundos>` — tráfico mezclado de los
tres sistemas (catálogo público, panel, POS, restaurante), sólo lecturas.

Medido el 2026-10-05 en la máquina de desarrollo contra MySQL local (40 usuarios
concurrentes, 30 s): **523 requests/s, 0 errores, latencia p50 73 ms / p95 112 ms /
p99 143 ms**; el backend pasó de 378 a 491 MB de RAM.

Estimación para el VPS-1 (2 vCores, más lento que la máquina de desarrollo): del
orden de 150–250 requests/s con latencias parecidas. Una tienda chica genera, en sus
horas pico, 1–5 requests/s (un cliente navegando ≈ 1 request cada 2–3 s; un POS o un
restaurante en servicio, un poco más). **20 tiendas ≈ 20–100 requests/s en el peor
momento: entra con margen**. La memoria es el límite real: con `-Xmx1g` para el
backend, MySQL (~1 GB) y el sistema, los 4 GB alcanzan. Repetir la prueba contra el
VPS real después del primer deploy (desde otra máquina) y anotar acá los números.
