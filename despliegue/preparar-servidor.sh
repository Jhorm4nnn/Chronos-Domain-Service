#!/bin/bash
# Prepara una instancia EC2 con Ubuntu 24.04: Docker, Docker Compose y 2 GB de swap.
# Se puede pegar como "user data" al crear la instancia o correr una vez con sudo.
set -euo pipefail

apt-get update -y
apt-get install -y docker.io docker-compose-v2 git
systemctl enable --now docker
usermod -aG docker ubuntu

# Swap: compilar dos servicios Spring y correrlos junto a MySQL no cabe en 1-2 GB de RAM sin ella.
if [ ! -f /swapfile ]; then
    fallocate -l 2G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi
