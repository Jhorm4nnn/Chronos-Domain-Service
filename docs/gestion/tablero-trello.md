# Tablero del proyecto (Trello)

**Tablero:** [Chronos · Sistema de Turnos (Grupo 5)](https://trello.com/b/JqZOzIz4/chronos-sistema-de-turnos-grupo-5)
— público, de solo lectura para quien no es miembro.

Es el tablero de Scrum del proyecto: el backlog arquitectónico (A-01 a A-15) y las historias de usuario de los dos
servicios (14 del Domain Service y 7 de AuthCore). Su organización y la definición de terminado están explicadas en la
sección 10 de los [entregables de las semanas 9 y 10](../metodologias/Entregables_Semanas_9_10_Chronos_Grupo5.pdf).

## Cómo está organizado

| Lista | Qué contiene |
|---|---|
| Product Backlog | Ítems sin planificar y del siguiente sprint, en orden de prioridad |
| Sprint Backlog | Ítems del sprint vigente y una tarjeta fija con las metas de los sprints y la definición de terminado |
| En curso | Máximo un ítem por integrante |
| En revisión (PR) | Esperando la revisión del otro integrante en un pull request |
| Terminado | Cumple toda la definición de terminado |

| Etiqueta | Significado |
|---|---|
| Deuda técnica | Ítem del backlog arquitectónico que paga una deuda (D-01 a D-15) |
| Spike | Investigación con tiempo fijo que termina en una decisión (SP-1 a SP-6) |
| Historia · Domain Service / Historia · AuthCore | Historia de usuario del servicio correspondiente |
| Sprint 1 / Sprint 2 | Sprint en el que está planificado el ítem |
| Implementada, falta verificar | La historia está en el código pero aún no cumple la definición de terminado |

Cada tarjeta lleva en el título el ID y la dimensión; en la descripción, el origen y la prioridad; y como lista de
verificación, el criterio de terminado (ítems A) o los criterios de aceptación (historias).

## Contenido del tablero al 10 de noviembre de 2026

Esta copia sirve para revisar el tablero desde GitHub. El estado vigente es el de Trello.

### Sprint Backlog (sprint 1, 10 al 16 de noviembre)
- 📌 Cómo usar este tablero · Metas de los sprints · Definición de terminado
- A-01 · Spike SP-1 y wrapper de Gradle en AuthCore · Dim 20
- A-02 · GitHub Actions: compilar y probar los dos servicios en cada push y pull request · Dim 30
- A-03 · Ejecutar todas las pruebas, corregir fallos y agregar pruebas unitarias de AuthService · Dim 40
- A-04 · Credenciales del administrador inicial por variables de entorno, sin escribirlas en el log; rotar la contraseña de MySQL · Dim 20
- A-05 · Spike SP-2 y migraciones versionadas en los dos servicios · Dim 60

### Product Backlog
Backlog arquitectónico:
- A-06 · Política de contraseñas y registro con correo según HU-1 de AuthCore · Dim 20 (sprint 2)
- A-07 · Llevar la regla «turno vigente» al dominio (EstadoTurno) y usarla en el adaptador y en el doble · Dim 30 (sprint 2)
- A-08 · Unificar la numeración de HU en README, requests.http y los dos backlogs · Dim 20 (sprint 2)
- A-09 · Spike SP-5 y medición de tiempos; cierra el criterio 5 de HU-7 · Dim 30 (sprint 2)
- A-10 · Spike SP-6 y documentación OpenAPI · Dim 30 (sprint 2)
- A-11 · Spike SP-3 y ADR sobre la firma RS256 · Dim 50
- A-12 · Reintento o mensaje claro ante conflictos de concurrencia · Dim 30
- A-13 · Spike SP-4 y ADR de revocación de tokens · Dim 50
- A-14 · Paginación en los listados · Dim 40
- A-15 · Alinear versiones de Java y Spring Boot según SP-1 · Dim 60

Historias de usuario, por prioridad (P) y dimensión:

| Historia | Servicio | P | Dim. | Etiqueta |
|---|---|---|---|---|
| HU-9 · Registrar clientes | Domain Service | 10 | 10 | Implementada, falta verificar |
| HU-1 · Registrar profesionales | Domain Service | 10 | 20 | Implementada, falta verificar |
| HU-1 · Registrarme con username/email y password | AuthCore | 10 | 20 | Sprint 2 |
| HU-2 · Iniciar sesión con mis credenciales | AuthCore | 10 | 20 | Implementada, falta verificar |
| HU-4 · Restringir el acceso a los endpoints según el rol | AuthCore | 9 | 40 | Implementada, falta verificar |
| HU-2 · Crear un turno indicando cliente, profesional, fecha y hora | Domain Service | 9 | 50 | Implementada, falta verificar |
| HU-3 · Asignar un rol a un usuario existente | AuthCore | 8 | 30 | Implementada, falta verificar |
| HU-3 · Validar que no haya cruces de horario al agendar | Domain Service | 8 | 50 | Implementada, falta verificar |
| HU-5 · Contraseñas encriptadas con BCrypt | AuthCore | 7 | 20 | Implementada, falta verificar |
| HU-14 · Lógica de negocio sin duplicar entre adaptadores | Domain Service | 7 | 30 | Implementada, falta verificar |
| HU-6 · JWT con firma digital y expiración configurable | AuthCore | 7 | 30 | Implementada, falta verificar |
| HU-6 · Lógica de negocio desacoplada de la interfaz de entrada | Domain Service | 7 | 50 | Implementada, falta verificar |
| HU-4 · Cancelar un turno existente | Domain Service | 6 | 10 | Implementada, falta verificar |
| HU-10 · Reprogramar un turno existente | Domain Service | 6 | 20 | Implementada, falta verificar |
| HU-7 · Separación estricta entre dominio e infraestructura | AuthCore | 6 | 50 | Implementada, falta verificar |
| HU-5 · Consultar los turnos por profesional | Domain Service | 5 | 10 | Implementada, falta verificar |
| HU-11 · Consultar los turnos por cliente | Domain Service | 5 | 10 | Implementada, falta verificar |
| HU-12 · Consultar los turnos por rango de fechas | Domain Service | 5 | 10 | Implementada, falta verificar |
| HU-7 · Operaciones de turnos en menos de 1 segundo | Domain Service | 4 | 30 | Sprint 2 |
| HU-8 · Mensajes de error claros | Domain Service | 3 | 10 | Implementada, falta verificar |
| HU-13 · Errores sin detalles técnicos internos | Domain Service | 3 | 10 | Implementada, falta verificar |

### En curso, En revisión (PR) y Terminado
Sin tarjetas.
