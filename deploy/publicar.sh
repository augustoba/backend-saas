#!/usr/bin/env bash
# Publica una versión nueva en el VPS (correr desde tu máquina, en Git Bash).
#   ./deploy/publicar.sh root@IP_DEL_VPS
# Compila backend y front acá (más simple que compilar en el servidor), sube
# los dos y reinicia el servicio. Las migraciones de Flyway corren solas al
# arrancar el backend; si una falla, el backend no levanta y el anterior
# (backend.jar.anterior) queda a mano para volver atrás.
set -euo pipefail

DESTINO="${1:?Uso: ./deploy/publicar.sh usuario@ip}"
RAIZ="$(cd "$(dirname "$0")/../.." && pwd)"

echo "== Backend: tests y jar"
(cd "$RAIZ/back" && ./mvnw -q clean package)
JAR=$(ls "$RAIZ"/back/target/*.jar | grep -v plain | head -1)

echo "== Front: build de producción"
(cd "$RAIZ/front" && npm ci --silent && npm run build)
FRONT="$RAIZ/front/dist/ecommerce-ninos/browser"

echo "== Subiendo"
ssh "$DESTINO" 'mkdir -p /tmp/saasweb-nuevo/front'
scp -q "$JAR" "$DESTINO:/tmp/saasweb-nuevo/backend.jar"
scp -q -r "$FRONT"/* "$DESTINO:/tmp/saasweb-nuevo/front/"

echo "== Instalando y reiniciando"
ssh "$DESTINO" 'set -e
  cd /srv/saasweb
  [ -f backend.jar ] && cp backend.jar backend.jar.anterior
  mv /tmp/saasweb-nuevo/backend.jar backend.jar
  rm -rf front.anterior && [ -d front ] && mv front front.anterior
  mv /tmp/saasweb-nuevo/front front
  chown -R saasweb:saasweb /srv/saasweb
  systemctl restart saasweb
  for i in $(seq 1 60); do
    if curl -sf -o /dev/null http://127.0.0.1:8080/api/settings; then echo "backend arriba"; exit 0; fi
    sleep 2
  done
  echo "El backend no respondió en 2 minutos: journalctl -u saasweb -n 100"; exit 1'
echo "== Listo"
