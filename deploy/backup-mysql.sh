#!/usr/bin/env bash
# Backup diario de la base del SaaS (todas las tiendas en un solo dump).
# Cron (root):  15 4 * * *  /srv/saasweb/backup-mysql.sh >> /var/log/saasweb-backup.log 2>&1
#
# - --single-transaction: copia consistente sin frenar a las tiendas (InnoDB).
# - Guarda 14 días en el servidor y, si hay rclone configurado con el remoto
#   "backup" (ej: un bucket de OVH Object Storage o Cloudflare R2), sube una
#   copia afuera: un backup que vive sólo en el mismo VPS no sirve si se rompe el VPS.
# - Credenciales en /root/.my.cnf ([mysqldump] user=... password=...), no acá.
set -euo pipefail

DB=saasweb
DIR=/var/backups/saasweb
DIAS=14
FECHA=$(date +%F_%H%M)
ARCHIVO="$DIR/${DB}_${FECHA}.sql.gz"

mkdir -p "$DIR"
mysqldump --single-transaction --routines --triggers --set-gtid-purged=OFF "$DB" | gzip -9 > "$ARCHIVO.tmp"
mv "$ARCHIVO.tmp" "$ARCHIVO"
echo "$(date -Is) backup ok: $ARCHIVO ($(du -h "$ARCHIVO" | cut -f1))"

find "$DIR" -name "${DB}_*.sql.gz" -mtime +$DIAS -delete

if command -v rclone >/dev/null 2>&1 && rclone listremotes | grep -q '^backup:'; then
  rclone copy "$ARCHIVO" "backup:saasweb-backups/" && echo "$(date -Is) copia externa ok"
fi
