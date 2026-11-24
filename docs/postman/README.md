# Colección de Postman: guía de contratos

Las nueve operaciones de la guía de contratos del profesor para el Sistema de Turnos, en su mismo orden. Todas existen en
el Domain Service con el mismo verbo, ruta y parámetros, así que no hubo que cambiar el código.

| Operación de la guía | Controlador | Respuesta correcta | Errores comprobados |
|---|---|---|---|
| `POST /api/clientes` | `ClienteController` | 201 con `Location` | 400, 401, 403, 409 |
| `GET /api/clientes` | `ClienteController` | 200 | 401, 403 |
| `POST /api/profesionales` | `ProfesionalController` | 201 con `Location` | 400, 403, 409 |
| `GET /api/profesionales` | `ProfesionalController` | 200 | 401 |
| `POST /api/turnos` | `TurnoController` | 201 con `Location` | 400, 401, 403, 404, 409 |
| `GET /api/turnos?profesionalId=&clienteId=&desde=&hasta=&incluirCancelados=` | `TurnoController` | 200 (también con los filtros vacíos) | 400, 401, 404 |
| `GET /api/turnos/{id}` | `TurnoController` | 200 | 403, 404 |
| `PUT /api/turnos/{id}/horario` | `TurnoController` | 200 | 400, 403, 404, 409 |
| `POST /api/turnos/{id}/cancelacion` | `TurnoController` | 200 | 401, 404, 409 |

Todas exigen un token ADMIN de AuthCore. El servicio ofrece además `GET /api/clientes/{id}` y
`GET /api/profesionales/{id}`, que la guía no pide.

## Cómo usarla

1. En Postman, **Import**: la colección y el ambiente (`Chronos AWS` o `Chronos Local`).
2. Elija el ambiente y escriba `admin_password` en *Current value*. No se guarda en estos archivos.
3. Clic derecho en la colección, **Run**. La carpeta 0 obtiene `{{token}}` y `{{token_user}}`.

Cada corrida crea un usuario, un cliente, un profesional y dos turnos de prueba.

## Última corrida

24 de noviembre de 2026, con la CLI de Postman 1.75.0 contra AWS: **43 peticiones, 90 comprobaciones, 0 fallos**. La salida
completa, sin tokens ni contraseñas, está en [`corrida-aws-2026-11-24.txt`](corrida-aws-2026-11-24.txt).
