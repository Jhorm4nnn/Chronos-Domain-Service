# Antecedentes del proyecto

Documentos traídos del repositorio original
[Crhonos-Sistema_de_Turnos](https://github.com/Jhorm4nnn/Crhonos-Sistema_de_Turnos). Se conservan como historia
del proyecto. **Lo vigente es el código de este repositorio y de
[Chronos-AuthCore](https://github.com/Jhorm4nnn/Chronos-AuthCore), la guía y el product backlog de [`docs/`](../).**

| Documento | Qué contiene | Qué ya no aplica |
|---|---|---|
| `SISTEMA DE TURNOS INTRODUCCION.pdf` | Presentación inicial del grupo: problema, sistema y requisitos | La lista de requisitos, reemplazada por el product backlog actual |
| `REVISION_1_PRODUCT_BACKLOG_CORREGIDO_FINAL.xlsx` | Backlog de la revisión 1, con datos del grupo 5 | RF, RNF y HU: su numeración y su alcance cambiaron |
| `Arquitectura lógica del domain-service - Sistema de Generación de Turnos.docx` | Evolución POO → MVC → capas → hexagonal, comparación de arquitecturas, SOLID y resultados de pruebas de esas fases | Ver las diferencias de abajo |

## Diferencias entre el documento de arquitectura y lo vigente

| Tema | Documento de arquitectura | Vigente |
|---|---|---|
| Datos del cliente | Nombre completo, documento, teléfono, correo y género | Nombre, teléfono y correo opcional |
| Unicidad | Documento | Teléfono del cliente y nombre del profesional |
| Persistencia | Archivos de texto | JPA con H2 (desarrollo) y MySQL (producción) |
| Interfaces | Ventanas Swing, consola y API | Consola y API REST |
| Firma del JWT | HS256 | HS384 |
| Permisos de consulta | Cualquier usuario autenticado | Solo ADMIN |
| Reglas de turno | Clases `ReglaTurno` | `ValidadorAgenda` y métodos del dominio |

El código de las fases POO y de capas no está aquí: sigue en el historial de git del repositorio original.
