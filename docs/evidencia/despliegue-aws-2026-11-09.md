# Evidencia: prueba de punta a punta contra AWS

- Fecha: 2026-11-10 02:36 UTC
- Servidor: instancia EC2 `chronos` (t3.small, Ubuntu 24.04, us-east-1), IP pública <ip-publica>
- AuthCore en el puerto 8081 y Domain Service en el 8082; MySQL sin puerto publicado
- Tokens y contraseñas ocultos. El token ADMIN es del administrador inicial creado desde ADMIN_USERNAME/ADMIN_PASSWORD.

## 1. AuthCore
### Login del administrador -> JWT
```
$ curl -X POST http://<ip-publica>:8081/api/auth/login
HTTP 200 (esperado 200) -> OK
{"token":"<JWT oculto>"}
```

### Registro de un usuario normal (rol USER)
```
$ curl -X POST http://<ip-publica>:8081/api/auth/register
HTTP 201 (esperado 201) -> OK
{"id":3,"username":"usuario78212"}
```

### Registro con contraseña débil -> 400 (HU-1)
```
$ curl -X POST http://<ip-publica>:8081/api/auth/register
HTTP 400 (esperado 400) -> OK
{"error":"password: password debe tener al menos 8 caracteres e incluir letras y numeros"}
```

### Login del usuario normal -> JWT con rol USER
```
$ curl -X POST http://<ip-publica>:8081/api/auth/login
HTTP 200 (esperado 200) -> OK
{"token":"<JWT oculto>"}
```

### Login con contraseña incorrecta -> 401
```
$ curl -X POST http://<ip-publica>:8081/api/auth/login
HTTP 401 (esperado 401) -> OK
{"error":"Usuario o contrasena incorrectos"}
```

### Listar usuarios sin token -> 401 (HU-4)
```
$ curl -X GET http://<ip-publica>:8081/api/auth/users
HTTP 401 (esperado 401) -> OK
{"error":"No autenticado: falta un token valido"}
```

### Listar usuarios con token USER -> 403 (HU-4)
```
$ curl -X GET http://<ip-publica>:8081/api/auth/users -H "Authorization: Bearer <token>"
HTTP 403 (esperado 403) -> OK
{"error":"No tiene permiso para esta operacion"}
```

## 2. Domain Service con token ADMIN
### Registrar un cliente
```
$ curl -X POST http://<ip-publica>:8082/api/clientes -H "Authorization: Bearer <token>"
HTTP 201 (esperado 201) -> OK
{"id":1,"nombre":"Ana Pérez 78212","telefono":"300782125","correo":"ana78212@correo.com"}
```

### Registrar un profesional (lunes 08:00-12:00)
```
$ curl -X POST http://<ip-publica>:8082/api/profesionales -H "Authorization: Bearer <token>"
HTTP 201 (esperado 201) -> OK
{"id":1,"nombre":"Dra. López 78212","disponibilidad":[{"dia":"MONDAY","desde":"08:00:00","hasta":"12:00:00"}]}
```

### Crear un turno el lunes 2026-11-16 a las 09:00 (30 min)
```
$ curl -X POST http://<ip-publica>:8082/api/turnos -H "Authorization: Bearer <token>"
HTTP 201 (esperado 201) -> OK
{"id":1,"estado":"AGENDADO","clienteId":1,"cliente":"Ana Pérez 78212","profesionalId":1,"profesional":"Dra. López 78212","fecha":"2026-11-16","horaInicio":"09:00:00","horaFin":"09:30:00","duracionMinutos":30}
```

### Provocar un cruce: 09:15 el mismo día -> 409 con el turno en conflicto
```
$ curl -X POST http://<ip-publica>:8082/api/turnos -H "Authorization: Bearer <token>"
HTTP 409 (esperado 409) -> OK
{"status":409,"error":"Conflict","mensaje":"El horario se cruza con el turno 1 del cliente Ana Pérez 78212 (2026-11-16 09:00 a 09:30)","detalles":["turno en conflicto: 1"]}
```

### Turno pegado al anterior (09:30): no se cruza -> 201
```
$ curl -X POST http://<ip-publica>:8082/api/turnos -H "Authorization: Bearer <token>"
HTTP 201 (esperado 201) -> OK
{"id":2,"estado":"AGENDADO","clienteId":1,"cliente":"Ana Pérez 78212","profesionalId":1,"profesional":"Dra. López 78212","fecha":"2026-11-16","horaInicio":"09:30:00","horaFin":"10:00:00","duracionMinutos":30}
```

### Reprogramar el primer turno a las 10:00
```
$ curl -X PUT http://<ip-publica>:8082/api/turnos/1/horario -H "Authorization: Bearer <token>"
HTTP 200 (esperado 200) -> OK
{"id":1,"estado":"AGENDADO","clienteId":1,"cliente":"Ana Pérez 78212","profesionalId":1,"profesional":"Dra. López 78212","fecha":"2026-11-16","horaInicio":"10:00:00","horaFin":"10:30:00","duracionMinutos":30}
```

### Cancelar el primer turno
```
$ curl -X POST http://<ip-publica>:8082/api/turnos/1/cancelacion -H "Authorization: Bearer <token>"
HTTP 200 (esperado 200) -> OK
{"id":1,"estado":"CANCELADO","clienteId":1,"cliente":"Ana Pérez 78212","profesionalId":1,"profesional":"Dra. López 78212","fecha":"2026-11-16","horaInicio":"10:00:00","horaFin":"10:30:00","duracionMinutos":30}
```

### Cancelarlo otra vez -> 409
```
$ curl -X POST http://<ip-publica>:8082/api/turnos/1/cancelacion -H "Authorization: Bearer <token>"
HTTP 409 (esperado 409) -> OK
{"status":409,"error":"Conflict","mensaje":"El turno 1 ya está cancelado","detalles":[]}
```

### Consultar por profesional y rango de fechas (excluye cancelados)
```
$ curl -X GET http://<ip-publica>:8082/api/turnos?profesionalId=1&desde=2026-11-16&hasta=2026-11-16 -H "Authorization: Bearer <token>"
HTTP 200 (esperado 200) -> OK
[{"id":2,"estado":"AGENDADO","clienteId":1,"cliente":"Ana Pérez 78212","profesionalId":1,"profesional":"Dra. López 78212","fecha":"2026-11-16","horaInicio":"09:30:00","horaFin":"10:00:00","duracionMinutos":30}]
```

### Consultar por cliente incluyendo cancelados
```
$ curl -X GET http://<ip-publica>:8082/api/turnos?clienteId=1&incluirCancelados=true -H "Authorization: Bearer <token>"
HTTP 200 (esperado 200) -> OK
[{"id":2,"estado":"AGENDADO","clienteId":1,"cliente":"Ana Pérez 78212","profesionalId":1,"profesional":"Dra. López 78212","fecha":"2026-11-16","horaInicio":"09:30:00","horaFin":"10:00:00","duracionMinutos":30},{"id":1,"estado":"CANCELADO","clienteId":1,"cliente":"Ana Pérez 78212","profesionalId":1,"profesional":"Dra. López 78212","fecha":"2026-11-16","horaInicio":"10:00:00","horaFin":"10:30:00
```

## 3. Domain Service: permisos
### Consultar turnos con token USER -> 403
```
$ curl -X GET http://<ip-publica>:8082/api/turnos -H "Authorization: Bearer <token>"
HTTP 403 (esperado 403) -> OK
{"status":403,"mensaje":"No tiene permiso para esta operación"}
```

### Consultar turnos sin token -> 401
```
$ curl -X GET http://<ip-publica>:8082/api/turnos
HTTP 401 (esperado 401) -> OK
{"status":401,"mensaje":"No autenticado: falta un token válido"}
```

### Consultar turnos con un token falso -> 401
```
$ curl -X GET http://<ip-publica>:8082/api/turnos -H "Authorization: Bearer <token>"
HTTP 401 (esperado 401) -> OK
{"status":401,"mensaje":"No autenticado: falta un token válido"}
```

**Resultado: 20 pasos OK, 0 con fallo.**
