#!/bin/bash
# MySQL lo ejecuta una sola vez, al crear el volumen de datos.
# Una base y un usuario por servicio: cada aplicación solo puede tocar sus propias tablas.
set -euo pipefail

mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <<SQL
CREATE DATABASE IF NOT EXISTS authcore_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS turnos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'authcore'@'%' IDENTIFIED BY '${AUTHCORE_DB_PASSWORD}';
CREATE USER IF NOT EXISTS 'turnos'@'%' IDENTIFIED BY '${TURNOS_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON authcore_db.* TO 'authcore'@'%';
GRANT ALL PRIVILEGES ON turnos.* TO 'turnos'@'%';
FLUSH PRIVILEGES;
SQL
