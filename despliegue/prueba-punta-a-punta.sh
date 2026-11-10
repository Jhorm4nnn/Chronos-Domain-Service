#!/bin/bash
# Prueba de punta a punta contra el despliegue en AWS. Escribe la evidencia sin tokens ni contraseñas.
# Uso: ./prueba-punta-a-punta.sh <ip-publica> <archivo-de-evidencia.md>
# Lee el administrador inicial del .env del servidor por SSH (llave en $SSH_KEY), sin mostrarlo.
set -u
IP="$1"; SALIDA="$2"
SSH_KEY="${SSH_KEY:-$HOME/.ssh/chronos-aws}"
PY=$(command -v python3 || command -v py)
AUTH="http://$IP:8081"; DOM="http://$IP:8082"
LUNES=$(date -d "next monday" +%F)   # el profesional de prueba atiende los lunes
SUF=$(date +%s | tail -c 6)

ADMIN_USER=$(ssh -i "$SSH_KEY" -o BatchMode=yes ubuntu@$IP 'grep ^ADMIN_USERNAME= ~/chronos/Chronos-Domain-Service/despliegue/.env | cut -d= -f2-')
ADMIN_PASS=$(ssh -i "$SSH_KEY" -o BatchMode=yes ubuntu@$IP 'grep ^ADMIN_PASSWORD= ~/chronos/Chronos-Domain-Service/despliegue/.env | cut -d= -f2-')

ok=0; fallos=0
oculta() { sed -E 's/eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+/<JWT oculto>/g'; }
json() { "$PY" -c "import sys,json; d=json.load(sys.stdin); print(eval(sys.argv[1]))" "$1"; }

# paso <descripción> <esperado> <método> <url> [token] [cuerpo]
paso() {
    local desc="$1" esperado="$2" metodo="$3" url="$4" token="${5:-}" cuerpo="${6:-}"
    local args=(-s -m 20 -o /tmp/e2e_cuerpo -w '%{http_code}' -X "$metodo")
    [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
    # El cuerpo va en un archivo UTF-8: en Windows, curl recibe los argumentos con la codificación del sistema.
    if [ -n "$cuerpo" ]; then printf '%s' "$cuerpo" > /tmp/e2e_peticion; args+=(-H 'Content-Type: application/json; charset=utf-8' --data-binary @/tmp/e2e_peticion); fi
    CODIGO=$(curl "${args[@]}" "$url")
    CUERPO=$(cat /tmp/e2e_cuerpo)
    local marca="OK"; if [ "$CODIGO" = "$esperado" ]; then ok=$((ok+1)); else marca="FALLO"; fallos=$((fallos+1)); fi
    local ruta="${url#http://$IP}"
    {
        echo "### $desc"
        echo '```'
        echo "$ curl -X $metodo http://<ip-publica>${ruta}$( [ -n "$token" ] && echo ' -H "Authorization: Bearer <token>"')"
        echo "HTTP $CODIGO (esperado $esperado) -> $marca"
        echo "$CUERPO" | oculta | cut -c1-400
        echo '```'
        echo
    } >> "$SALIDA"
}

{
    echo "# Evidencia: prueba de punta a punta contra AWS"
    echo
    echo "- Fecha: $(date -u '+%Y-%m-%d %H:%M UTC')"
    echo "- Servidor: instancia EC2 \`chronos\` (t3.small, Ubuntu 24.04, us-east-1), IP pública <ip-publica>"
    echo "- AuthCore en el puerto 8081 y Domain Service en el 8082; MySQL sin puerto publicado"
    echo "- Tokens y contraseñas ocultos. El token ADMIN es del administrador inicial creado desde ADMIN_USERNAME/ADMIN_PASSWORD."
    echo
} > "$SALIDA"

echo "## 1. AuthCore" >> "$SALIDA"
paso "Login del administrador -> JWT" 200 POST "$AUTH/api/auth/login" "" "{\"username\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASS\"}"
TOKEN_ADMIN=$(echo "$CUERPO" | json 'd["token"]')
USUARIO="usuario$SUF"; CLAVE_USUARIO="Prueba${SUF}x9"
paso "Registro de un usuario normal (rol USER)" 201 POST "$AUTH/api/auth/register" "" "{\"username\":\"$USUARIO\",\"password\":\"$CLAVE_USUARIO\"}"
paso "Registro con contraseña débil -> 400 (HU-1)" 400 POST "$AUTH/api/auth/register" "" "{\"username\":\"debil$SUF\",\"password\":\"corta\"}"
paso "Login del usuario normal -> JWT con rol USER" 200 POST "$AUTH/api/auth/login" "" "{\"username\":\"$USUARIO\",\"password\":\"$CLAVE_USUARIO\"}"
TOKEN_USER=$(echo "$CUERPO" | json 'd["token"]')
paso "Login con contraseña incorrecta -> 401" 401 POST "$AUTH/api/auth/login" "" "{\"username\":\"$USUARIO\",\"password\":\"Incorrecta123\"}"
paso "Listar usuarios sin token -> 401 (HU-4)" 401 GET "$AUTH/api/auth/users"
paso "Listar usuarios con token USER -> 403 (HU-4)" 403 GET "$AUTH/api/auth/users" "$TOKEN_USER"

echo "## 2. Domain Service con token ADMIN" >> "$SALIDA"
paso "Registrar un cliente" 201 POST "$DOM/api/clientes" "$TOKEN_ADMIN" "{\"nombre\":\"Ana Pérez $SUF\",\"telefono\":\"300$SUF$((RANDOM%10))\",\"correo\":\"ana$SUF@correo.com\"}"
CLIENTE=$(echo "$CUERPO" | json 'd["id"]')
paso "Registrar un profesional (lunes 08:00-12:00)" 201 POST "$DOM/api/profesionales" "$TOKEN_ADMIN" "{\"nombre\":\"Dra. López $SUF\",\"disponibilidad\":[{\"dia\":\"MONDAY\",\"desde\":\"08:00\",\"hasta\":\"12:00\"}]}"
PROF=$(echo "$CUERPO" | json 'd["id"]')
paso "Crear un turno el lunes $LUNES a las 09:00 (30 min)" 201 POST "$DOM/api/turnos" "$TOKEN_ADMIN" "{\"clienteId\":$CLIENTE,\"profesionalId\":$PROF,\"fecha\":\"$LUNES\",\"hora\":\"09:00\"}"
TURNO=$(echo "$CUERPO" | json 'd["id"]')
paso "Provocar un cruce: 09:15 el mismo día -> 409 con el turno en conflicto" 409 POST "$DOM/api/turnos" "$TOKEN_ADMIN" "{\"clienteId\":$CLIENTE,\"profesionalId\":$PROF,\"fecha\":\"$LUNES\",\"hora\":\"09:15\"}"
paso "Turno pegado al anterior (09:30): no se cruza -> 201" 201 POST "$DOM/api/turnos" "$TOKEN_ADMIN" "{\"clienteId\":$CLIENTE,\"profesionalId\":$PROF,\"fecha\":\"$LUNES\",\"hora\":\"09:30\"}"
paso "Reprogramar el primer turno a las 10:00" 200 PUT "$DOM/api/turnos/$TURNO/horario" "$TOKEN_ADMIN" "{\"hora\":\"10:00\"}"
paso "Cancelar el primer turno" 200 POST "$DOM/api/turnos/$TURNO/cancelacion" "$TOKEN_ADMIN"
paso "Cancelarlo otra vez -> 409" 409 POST "$DOM/api/turnos/$TURNO/cancelacion" "$TOKEN_ADMIN"
paso "Consultar por profesional y rango de fechas (excluye cancelados)" 200 GET "$DOM/api/turnos?profesionalId=$PROF&desde=$LUNES&hasta=$LUNES" "$TOKEN_ADMIN"
paso "Consultar por cliente incluyendo cancelados" 200 GET "$DOM/api/turnos?clienteId=$CLIENTE&incluirCancelados=true" "$TOKEN_ADMIN"

echo "## 3. Domain Service: permisos" >> "$SALIDA"
paso "Consultar turnos con token USER -> 403" 403 GET "$DOM/api/turnos" "$TOKEN_USER"
paso "Consultar turnos sin token -> 401" 401 GET "$DOM/api/turnos"
paso "Consultar turnos con un token falso -> 401" 401 GET "$DOM/api/turnos" "token.falso.123"

echo "**Resultado: $ok pasos OK, $fallos con fallo.**" >> "$SALIDA"
echo "OK=$ok FALLOS=$fallos"
