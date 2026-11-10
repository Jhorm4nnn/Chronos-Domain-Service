# Domain Service — Sistema de Turnos (Ingeniería del Software III, Universidad de Pamplona)

Servicio de dominio que se apoya en **AuthCore** (JWT HS384) para autenticar. Gestiona clientes, profesionales
con disponibilidad semanal y turnos que **nunca se solapan** para un mismo profesional.

**El problema:** muchos negocios pequeños agendan citas por teléfono o en un cuaderno, lo que genera choques de
horario y pérdida de clientes. Chronos es el sistema de turnos que lo evita.

Paquete base: `com.sistematurnos.agenda` · Java 17 · Spring Boot 3.5.6 · Maven · H2 (dev) / MySQL (prod) · JUnit 5.
El grupo no declaró tecnologías: se usó el estándar del curso (decisión documentada en el Excel, hoja de notas).

## Estado honesto

| Parte | Estado |
|---|---|
| Dominio + aplicación + CLI | Compilado y probado |
| Infraestructura (Spring, JPA, REST, seguridad) | Compilada y probada |
| `./mvnw verify` desde un clon limpio | **102 pruebas, 0 fallos** (9 de noviembre de 2026) |
| Despliegue en AWS (EC2 + Docker Compose) | Funcionando; prueba de punta a punta **20/20** ([evidencia](docs/evidencia/despliegue-aws-2026-11-09.md)) |

### Qué falló en el primer `mvn verify` y cómo se corrigió

La primera ejecución completa dio 102 pruebas con 2 fallos y 11 errores, por tres causas:

| Falla | Causa | Corrección |
|---|---|---|
| 11 errores en `*RepositoryAdapterContractTest` (`TransactionRequired`, violación de clave única) | `@DataJpaTest` solo envuelve en transacción los `@Test` declarados en la clase, no los heredados del contrato. Sin transacción, `em.flush()` fallaba y los datos de una prueba quedaban para la siguiente | Cada subclase JPA abre una transacción en `@BeforeEach` y la revierte en `@AfterEach`; los contratos siguen libres de Spring |
| `TurnosRestTest.consultas` | La prueba calculaba el próximo lunes y el próximo miércoles por separado; corriendo un lunes, el miércoles caía antes y el orden esperado se invertía | El miércoles es `lunes.plusDays(2)` |
| `ArquitecturaHexagonalTest.los_puertos_de_salida_son_interfaces` | `FiltroTurno` es un record (el dato que viaja por el puerto), no una interfaz | La regla excluye los records |

Además faltaban en GitHub `application/port/out` e `infrastructure/adapters/out`: la regla `out/` del `.gitignore`
excluía cualquier carpeta con ese nombre. Se cambió por `/out/` y se subieron los 23 archivos.

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

## Interfaz web

El servicio trae una interfaz para operar la agenda desde el navegador: es otro adaptador de entrada sobre la misma
API (RNF-01), igual que la consola. Se abre en `http://<host>:8082/` (en local, http://localhost:8082/).

| Pantalla | Qué permite |
|---|---|
| Inicio de sesión | Entrar con un usuario de AuthCore; el token se guarda solo durante la sesión del navegador |
| Agenda | Turnos en lista o en vista semanal, con filtros por profesional, cliente, fechas y cancelados; reprogramar y cancelar |
| Nuevo turno | Agendar mostrando la disponibilidad del profesional; los cruces (409) y los horarios fuera de disponibilidad se explican en pantalla |
| Clientes y Profesionales | Registrar y listar; los profesionales con sus franjas semanales |

Son archivos estáticos (`src/main/resources/static`, HTML, CSS y JavaScript sin dependencias): solo ellos son públicos,
la API sigue exigiendo un token ADMIN. El login llama a AuthCore en el mismo host y el puerto 8081 (se puede cambiar en
«Servidor de autenticación»); por eso AuthCore permite CORS para su `/api/**`.

## Ejecutar

No hace falta instalar Maven: `./mvnw` (o `mvnw.cmd` en Windows) lo descarga. Requiere JDK 17 o superior.

```bash
export JWT_SECRET_BASE64=<el mismo secreto Base64 que usa AuthCore>   # nunca en el repositorio
./mvnw verify                  # compila y corre todas las pruebas
./mvnw spring-boot:run         # REST en http://localhost:8082 (H2 en memoria)

# Consola (sin servidor web)
SPRING_PROFILES_ACTIVE=cli ./mvnw spring-boot:run
```

Producción: perfil `prod` con `DB_URL`, `DB_USER`, `DB_PASSWORD` como variables de entorno.
Pruebas manuales: `requests.http` (el token lo emite AuthCore; no lo guardes en el repositorio).

## Ejecutar en local (NetBeans u otro IDE)

1. Clone los dos repositorios, uno al lado del otro, y ábralos en NetBeans con *File > Open Project*
   (NetBeans reconoce el `pom.xml` de este y el `build.gradle` de AuthCore).
2. En la carpeta de cada proyecto cree un archivo `.env` (está en `.gitignore`; nunca se sube). La aplicación lo lee
   al arrancar, así que no hace falta configurar variables de entorno en el IDE. Los dos deben tener el **mismo**
   secreto (`openssl rand -base64 48`):
   ```properties
   JWT_SECRET_BASE64=<secreto Base64 de 48 bytes, el mismo de AuthCore>
   ```
3. *Run Project* en AuthCore (puerto 8081) y en este proyecto (puerto 8082). Sin perfil, este servicio usa H2 en
   memoria. La clase principal está en la propiedad `start-class` del `pom.xml`, que es la que usa NetBeans.
4. Pruebe con [`requests.http`](requests.http) o con
   `ADMIN_USERNAME=... ADMIN_PASSWORD=... ./despliegue/prueba-punta-a-punta.sh localhost evidencia.md`.

## Desplegar en AWS (EC2 + Docker Compose)

[`despliegue/docker-compose.yml`](despliegue/docker-compose.yml) levanta todo Chronos en una sola instancia:
MySQL (bases `authcore_db` y `turnos`, un usuario por servicio), AuthCore en el puerto 8081 y este servicio en el 8082,
los dos con el **mismo** `JWT_SECRET_BASE64`. MySQL no publica ningún puerto: solo lo ven las dos aplicaciones.

1. **Instancia EC2:** Ubuntu 24.04, mínimo 2 GB de RAM (por ejemplo `t3.small`) y 20 GB de disco.
   Como *user data*, pegue [`despliegue/preparar-servidor.sh`](despliegue/preparar-servidor.sh): instala Docker,
   Docker Compose y 2 GB de swap.
2. **Security group:**

   | Puerto | Origen | Para qué |
   |---|---|---|
   | 22 | Solo su IP (`x.x.x.x/32`) | SSH |
   | 8081 | `0.0.0.0/0` | AuthCore |
   | 8082 | `0.0.0.0/0` | Domain Service |
   | 3306 | **Ninguno** | MySQL nunca queda abierto a internet |

3. **Código:** los dos repositorios, uno al lado del otro, en la misma carpeta:
   ```bash
   mkdir chronos && cd chronos
   git clone https://github.com/Jhorm4nnn/Chronos-AuthCore.git
   git clone https://github.com/Jhorm4nnn/Chronos-Domain-Service.git
   ```
   Los repositorios son privados: en el servidor use un token de acceso de GitHub de solo lectura (que no se guarde
   en el historial del shell) o copie el código desde su equipo con `git archive` y `scp`.
4. **Secretos:** `./generar-env.sh` crea `despliegue/.env` con valores aleatorios y permisos 600. Ese archivo vive
   solo en el servidor; [`despliegue/.env.ejemplo`](despliegue/.env.ejemplo) muestra las variables sin valores.

   | Variable | Para qué |
   |---|---|
   | `JWT_SECRET_BASE64` | Secreto compartido por los dos servicios (`openssl rand -base64 48`) |
   | `MYSQL_ROOT_PASSWORD` | Root de MySQL |
   | `AUTHCORE_DB_PASSWORD`, `TURNOS_DB_PASSWORD` | Usuarios `authcore` y `turnos` de MySQL |
   | `ADMIN_USERNAME`, `ADMIN_PASSWORD` | Administrador inicial de AuthCore (8+ caracteres, letras y números) |

5. **Levantar:**
   ```bash
   cd Chronos-Domain-Service/despliegue
   ./generar-env.sh
   docker compose up -d --build
   docker compose ps            # mysql healthy; authcore y domain running
   ```
6. **Probar:** `POST http://<ip-publica>:8081/api/auth/login` con el administrador de `.env` devuelve el token;
   con él, `requests.http` funciona cambiando `@host` y `@authcore` por la IP pública.
   [`despliegue/prueba-punta-a-punta.sh`](despliegue/prueba-punta-a-punta.sh) recorre todo el flujo desde su equipo
   (login, cliente, profesional, turno, cruce 409, reprogramar, cancelar, filtros, 403 y 401) y escribe la evidencia
   sin tokens ni contraseñas: `./prueba-punta-a-punta.sh <ip-publica> evidencia.md`.

La base de datos se crea la primera vez con [`despliegue/mysql-init/01-crear-bases.sh`](despliegue/mysql-init/01-crear-bases.sh).
Si cambia las contraseñas de MySQL después, borre el volumen (`docker compose down -v`, que **borra los datos**) o
cámbielas también dentro de MySQL.

## Reglas de negocio clave

- Intervalos semiabiertos `[inicio, fin)`: un turno que empieza cuando otro termina **no** se cruza.
- Duración por defecto 30 min (rango 5–480); el turno debe caber en un solo día y en la disponibilidad del profesional.
- Solo se agendan turnos futuros; los pasados no se modifican. Los cancelados liberan el horario.
- Concurrencia: al agendar se bloquea la fila del profesional (`SELECT ... FOR UPDATE`), así dos solicitudes simultáneas se ordenan.
- Solo el rol `ADMIN` opera la API (`403` para `USER`, `401` sin token válido).
- Errores: 400 datos inválidos · 404 no existe · 409 duplicado/cruce/no disponible/estado inválido · 422 otra regla de dominio.

## Pendiente para el grupo

1. Probar el CLI con `SPRING_PROFILES_ACTIVE=cli`.
2. Decidir con el profesor los puntos abiertos de las observaciones del Excel (numeración de HU).
3. Reemplazar `ddl-auto: update` por migraciones versionadas (Flyway o Liquibase).

No incluye credenciales ni secretos: todo llega por variables de entorno.

## Gestión del proyecto

- **Tablero de Trello (Scrum):** [Chronos · Sistema de Turnos (Grupo 5)](https://trello.com/b/JqZOzIz4/chronos-sistema-de-turnos-grupo-5),
  público. Su organización y una copia de su contenido están en [`docs/gestion/tablero-trello.md`](docs/gestion/tablero-trello.md).
- **Product backlog:** [`docs/PRODUCT BACKLOG SISTEMA DE TURNOS.xlsx`](docs/PRODUCT%20BACKLOG%20SISTEMA%20DE%20TURNOS.xlsx).
- **Backlog arquitectónico, deuda técnica y sprints:** [`docs/metodologias/`](docs/metodologias/).

## Proyecto Chronos — repositorios relacionados

| Repositorio | Contenido |
|---|---|
| [Chronos-AuthCore](https://github.com/Jhorm4nnn/Chronos-AuthCore) | Emite el JWT (HU-1 a HU-7), puerto 8081 |
| **Chronos-Domain-Service** (este) | Agenda de turnos (HU-8 a HU-12), puerto 8082 |
| [Crhonos-Sistema_de_Turnos](https://github.com/Jhorm4nnn/Crhonos-Sistema_de_Turnos) | Repositorio original, reemplazado por estos dos |

Ambos servicios deben arrancar con **la misma** variable `JWT_SECRET_BASE64` (mínimo 48 bytes en Base64 para HS384).
La guía del profesor y el product backlog están en [`docs/`](docs/); los documentos anteriores del proyecto, en
[`docs/antecedentes/`](docs/antecedentes/LEEME.md).
Los entregables de metodologías (semanas 9 y 10) están en [`docs/metodologias/`](docs/metodologias/).
