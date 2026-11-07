# Domain Service — Sistema de Turnos (Ingeniería del Software III, Universidad de Pamplona)

Servicio de dominio que se apoya en **AuthCore** (JWT HS384) para autenticar. Gestiona clientes, profesionales
con disponibilidad semanal y turnos que **nunca se solapan** para un mismo profesional.

Paquete base: `com.sistematurnos.agenda` · Java 17 · Spring Boot 3.5.6 · Maven · H2 (dev) / MySQL (prod) · JUnit 5.
El grupo no declaró tecnologías: se usó el estándar del curso (decisión documentada en el Excel, hoja de notas).

## Estado honesto

| Parte | Estado |
|---|---|
| Dominio + aplicación + CLI | Compilado y probado: **66 pruebas puras** pasan |
| Infraestructura (Spring, JPA, REST, seguridad) | Escrita y revisada, **sin ejecutar** (sin acceso a Maven en el entorno de generación) |
| Pruebas Spring (contrato JPA, unicidad, REST de punta a punta) | Escritas, **sin ejecutar**: son la primera tarea del grupo con `mvn verify` |

## Arquitectura (hexagonal)

```
domain/          modelo, value objects, ValidadorAgenda (regla de no solapamiento), excepciones
application/     casos de uso (ClienteUseCase, ProfesionalUseCase, TurnoUseCase), servicios, puertos de salida
infrastructure/  adaptadores: REST (in), CLI (in), JPA (out), seguridad JWT, configuración
```

`domain` y `application` no importan Spring ni JPA (lo verifica `ArquitecturaHexagonalTest`).
La transacción se pide por el puerto `TransaccionPort`; el reloj se inyecta (`Clock`) para probar fechas.

## Historias de usuario → código

| HU | Qué hace | Dónde |
|---|---|---|
| HU-1..7 | Autenticación AuthCore | `infrastructure/security` (valida el JWT) |
| HU-8 | Registrar clientes y profesionales | `ClienteService`, `ProfesionalService`, `/api/clientes`, `/api/profesionales` |
| HU-9 | Crear turno | `TurnoService.agendar`, `POST /api/turnos` |
| HU-10 | No solapamiento | `RangoTiempo.seCruzaCon`, `ValidadorAgenda.buscarConflicto` |
| HU-11 | Cancelar / reprogramar | `POST /api/turnos/{id}/cancelacion`, `PUT /api/turnos/{id}/horario` |
| HU-12 | Consultar con filtros | `GET /api/turnos?profesionalId&clienteId&desde&hasta&incluirCancelados` |
| RNF-01 | Uso por consola | `TurnosCli` (perfil `cli`), mismos casos de uso que REST |

## Ejecutar

```bash
export JWT_SECRET_BASE64=<el mismo secreto Base64 que usa AuthCore>   # nunca en el repositorio
mvn verify                  # pruebas
mvn spring-boot:run         # REST en http://localhost:8082 (H2 en memoria)

# Consola (sin servidor web)
SPRING_PROFILES_ACTIVE=cli mvn spring-boot:run
```

Producción: perfil `prod` con `DB_URL`, `DB_USER`, `DB_PASSWORD` como variables de entorno.
Pruebas manuales: `requests.http` (el token lo emite AuthCore; no lo guardes en el repositorio).

## Reglas de negocio clave

- Intervalos semiabiertos `[inicio, fin)`: un turno que empieza cuando otro termina **no** se cruza.
- Duración por defecto 30 min (rango 5–480); el turno debe caber en un solo día y en la disponibilidad del profesional.
- Solo se agendan turnos futuros; los pasados no se modifican. Los cancelados liberan el horario.
- Concurrencia: al agendar se bloquea la fila del profesional (`SELECT ... FOR UPDATE`), así dos solicitudes simultáneas se ordenan.
- Solo el rol `ADMIN` opera la API (`403` para `USER`, `401` sin token válido).
- Errores: 400 datos inválidos · 404 no existe · 409 duplicado/cruce/no disponible/estado inválido · 422 otra regla de dominio.

## Pendiente para el grupo

1. Ejecutar `mvn verify` y corregir lo que aparezca en las pruebas Spring.
2. Ejecutar la prueba de punta a punta `TurnosRestTest` y adjuntar la evidencia.
3. Probar el CLI con `SPRING_PROFILES_ACTIVE=cli`.
4. Decidir con el profesor los puntos abiertos de las observaciones del Excel (numeración de HU).

No incluye credenciales ni secretos: todo llega por variables de entorno.

## Proyecto Chronos — repositorios relacionados

| Repositorio | Contenido |
|---|---|
| [Chronos-AuthCore](https://github.com/Jhorm4nnn/Chronos-AuthCore) | Emite el JWT (HU-1 a HU-7), puerto 8081 |
| **Chronos-Domain-Service** (este) | Agenda de turnos (HU-8 a HU-12), puerto 8082 |
| [Crhonos-Sistema_de_Turnos](https://github.com/Jhorm4nnn/Crhonos-Sistema_de_Turnos) | Repositorio original del proyecto y documentación |

Ambos servicios deben arrancar con **la misma** variable `JWT_SECRET_BASE64` (mínimo 48 bytes en Base64 para HS384).
La guía del profesor y el product backlog están en [`docs/`](docs/).
