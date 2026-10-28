# TASKS — Backend (Spring Boot)

Plan de implementación del backend del Atlas Interactivo de Cinesiterapia.
Cada task es un commit; al terminarla se marca `[x]` en ese mismo commit con el sufijo `[x.y]`.
Rama por task: `task-<n>-<descripcion-corta>`. Las HU referenciadas están en `docs/historias-usuario/`.

## Decisiones de arquitectura

| Tema | Decisión |
|---|---|
| Lenguaje / framework | Java 21, Spring Boot 3.x (web, security, data-jpa, validation, mail) |
| Base de datos | PostgreSQL en **Neon** (plan free), migraciones con Flyway |
| Autenticación | JWT: access token corto + refresh token rotativo guardado hasheado en BD (RNF_001) |
| Contraseñas | BCrypt (RNF_002); HTTPS en producción |
| Archivos (video/imágenes) | **Cloudflare R2** vía URL prefirmada (S3 API); nunca en disco del servidor |
| Correo (recuperar contraseña) | Resend o Brevo (plan free) |
| Despliegue | **Render** (web service free, Docker); Railway ya no es gratis |
| Docs de API | springdoc-openapi (`/swagger-ui`) |
| Tests | JUnit 5 + Testcontainers (Postgres) |
| Paquetes | Monolito modular: `auth`, `catalogo`, `ejercicio`, `media`, `comentario`, `metricas`, `common` |
| Borrado | Blando (`deleted_at`) para fichas y comentarios |

## Catálogos iniciales (seed, editables después)

**Categorías terapéuticas (9)** — propuesta, a validar con la docente:
1. Movilización pasiva
2. Movilización activo-asistida
3. Movilización activa libre
4. Ejercicio activo resistido
5. Estiramiento y elongación
6. Fortalecimiento isométrico
7. Propiocepción y equilibrio
8. Control motor y estabilización
9. Facilitación neuromuscular propioceptiva (FNP)

**Regiones anatómicas** — vista anterior: cabeza y cuello, hombro, brazo, codo, antebrazo, muñeca y mano, tórax, abdomen, cadera, muslo, rodilla, pierna, tobillo y pie.
Vista posterior: cabeza y cuello, columna cervical, hombro/escápula, brazo, codo, antebrazo, muñeca y mano, columna dorsal, columna lumbar, glúteo, muslo, rodilla, pierna, tobillo y pie.
Cada región guarda `svg_id` (id del path en el SVG del front). Sin distinción izquierda/derecha en la v1.

**Campos obligatorios para publicar (RF_025)** — propuesta: nombre, región, categoría, objetivo terapéutico, estructuras intervenidas, indicaciones, contraindicaciones, posición del paciente, posición del fisioterapeuta, ejecución, comandos verbales, dosificación (series, repeticiones, intensidad, tiempo, descanso, frecuencia), al menos 1 imagen o 1 video, estudiantes creadores, cohorte y profesora revisora. Opcionales: progresión y errores comunes.

**Otras decisiones por defecto:** cohorte como texto con formato `AAAA-N` (ej. `2025-1`); administradores creados por seed y luego promovidos por otro admin; lista de palabras ofensivas en BD con endpoint admin; video máx. 25 MB, imágenes máx. 5 MB y hasta 8 por ficha; comentario eliminado con respuestas se muestra como "comentario eliminado".

---

## Fase 0 — Fundación
- [x] 0.1 Scaffold Spring Boot (Maven, Java 21), perfiles `dev`/`prod`, CORS por variable de entorno
- [x] 0.2 Conexión a Neon (HikariCP pequeño, SSL), Flyway con migración vacía, `docker-compose` con Postgres para desarrollo local
- [x] 0.3 Manejo global de errores (`@ControllerAdvice`), formato de error uniforme, springdoc
- [x] 0.4 CI en GitHub Actions: build + tests
- [x] 0.5 Actualizar README con stack, variables de entorno y cómo correr

## Fase 1 — Autenticación (HU-16 a HU-20; RNF_001, RNF_002)
- [x] 1.1 Migración y entidad `usuario` (correo único, hash, rol de perfil, rol de sistema)
- [x] 1.2 `POST /auth/registro` con rol de perfil (HU-17)
- [x] 1.3 `POST /auth/login`, `/auth/refresh`, `/auth/logout` con JWT y refresh rotativo (HU-16)
- [x] 1.4 Filtro de seguridad, `@PreAuthorize` por rol, todo protegido salvo auth
- [x] 1.5 Recuperar contraseña: token de un solo uso con expiración + correo (HU-18)
- [x] 1.6 Cierre pendiente con periodo de gracia de 60 s: `POST /auth/cierre-pendiente`, cancelado por refresh (HU-19)
- [x] 1.7 Rate limit en login y registro
- [x] 1.8 Seed del primer administrador desde variables de entorno + endpoint para promover a admin

## Fase 2 — Catálogo y fichas (HU-04 a HU-15)
- [x] 2.1 Migraciones y entidades `region_anatomica`, `categoria_terapeutica` + seed
- [x] 2.2 Migración y entidad `ejercicio` con estado BORRADOR/PUBLICADO/ARCHIVADO y metadatos académicos
- [x] 2.3 Crear y actualizar ficha en borrador, validación mínima (HU-04 a HU-07, HU-09, HU-10)
- [x] 2.4 Entidad `media` y subida a R2 con URL prefirmada, validación de tipo/tamaño (HU-08)
- [x] 2.5 Validación de campos obligatorios y `POST /ejercicios/{id}/publicar` (HU-11)
- [x] 2.6 Editar ficha publicada (HU-12)
- [x] 2.7 Archivar / restaurar / eliminar (borrado blando) (HU-13, HU-14)
- [x] 2.8 Gestión compartida: cualquier admin lista y edita todas las fichas (HU-15)
- [x] 2.9 Tests de integración del ciclo de vida completo de la ficha

## Fase 3 — API de consulta del atlas (HU-01 a HU-03)
- [x] 3.1 `GET /regiones`, `GET /regiones/{id}/categorias`
- [x] 3.2 `GET /ejercicios?region&categoria` solo publicados (HU-01)
- [x] 3.3 `GET /ejercicios/{id}` ficha completa con media (HU-02)
- [x] 3.4 `GET /ejercicios/buscar?q=` con índice `pg_trgm` y búsqueda sin acentos (HU-03)
- [x] 3.5 Cabeceras de caché (ETag / Cache-Control) para ayudar al modo offline

## Fase 4 — Comentarios y moderación (HU-21 a HU-29)
- [x] 4.1 Entidades `comentario` y `reporte_comentario`, hilos de un nivel
- [x] 4.2 Crear y listar comentarios publicados (HU-21)
- [x] 4.3 Editar y eliminar comentario propio (HU-22, HU-23)
- [x] 4.4 Responder a un comentario (HU-29)
- [x] 4.5 Filtro de contenido ofensivo (normaliza acentos y sustituciones) → estado EN_REVISION; el autor ve su comentario atenuado (HU-24)
- [x] 4.6 Reportar comentario (HU-25)
- [x] 4.7 Cola de revisión para admin: ocultados + reportados (HU-26)
- [x] 4.8 Aceptar comentario y eliminar cualquier comentario como admin (HU-27, HU-28)
- [x] 4.9 CRUD admin de la lista de palabras ofensivas

## Fase 5 — Métricas (HU-30 a HU-34)
- [x] 5.1 Entidades `visita` y `sesion_uso`; registro de visitas y ping de sesión (RF_032)
- [x] 5.2 Acceso restringido a admin al módulo de métricas (HU-30)
- [x] 5.3 Visitas totales por periodo (HU-31)
- [x] 5.4 Ejercicios más consultados (HU-32)
- [x] 5.5 Usuarios activos por rol (HU-33)
- [x] 5.6 Tiempo promedio de uso por sesión (HU-34)

## Fase 6 — Calidad y seguridad (RNF_002, RNF_005)
- [ ] 6.1 Revisión de seguridad: cabeceras, validación de entrada, CORS estricto, secretos solo por entorno
- [ ] 6.2 Pruebas de carga ligeras (k6) para concurrencia
- [ ] 6.3 Cobertura de tests ≥ 80 % en servicios

## Fase 7 — Despliegue
- [x] 7.1 `Dockerfile` multi-stage y `.dockerignore`
- [ ] 7.2 Despliegue en Render con variables de entorno (`DATABASE_URL`, `JWT_SECRET`, `CORS_ORIGINS`, R2, correo)
- [x] 7.3 Endpoint `/actuator/health` y ping periódico (UptimeRobot) contra la suspensión del plan free
- [x] 7.4 Documentar el despliegue en el README
