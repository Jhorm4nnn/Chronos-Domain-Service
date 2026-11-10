#!/bin/bash
# Crea despliegue/.env con secretos aleatorios, SOLO en el servidor. Nunca se sube al repositorio.
# Si .env ya existe no lo toca: cambiar el secreto JWT invalida los tokens y cambiar las claves
# de MySQL rompe el acceso a un volumen ya creado.
set -euo pipefail
cd "$(dirname "$0")"

if [ -f .env ]; then
    echo ".env ya existe; no se modifica."
    exit 0
fi

umask 077
{
    echo "JWT_SECRET_BASE64=$(openssl rand -base64 48 | tr -d '\n')"
    echo "MYSQL_ROOT_PASSWORD=$(openssl rand -hex 24)"
    echo "AUTHCORE_DB_PASSWORD=$(openssl rand -hex 24)"
    echo "TURNOS_DB_PASSWORD=$(openssl rand -hex 24)"
    echo "ADMIN_USERNAME=admin"
    # Cumple la política de HU-1: más de 8 caracteres, con letras y números.
    echo "ADMIN_PASSWORD=Chronos$(openssl rand -hex 10)9"
} > .env
echo ".env creado con permisos 600. La contraseña del administrador está en ADMIN_PASSWORD."
