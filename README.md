# Atlas Interactivo de Cinesiterapia - Backend

API REST del atlas interactivo de ejercicios de cinesiterapia, organizada por region anatomica y categoria terapeutica. Permite a docentes y administradores crear fichas detalladas de ejercicios y a estudiantes/usuarios consultarlas, comentarlas y buscarlas.

El plan de trabajo esta en [`TASKS.md`](TASKS.md); los requerimientos en [`docs/requerimientos.md`](docs/requerimientos.md) y las historias de usuario en [`docs/historias-usuario/`](docs/historias-usuario/).

## Stack

| Tema | Tecnologia |
|---|---|
| Lenguaje / framework | Java 21, Spring Boot 3.5 (web, security, data-jpa, validation, mail, actuator) |
| Base de datos | PostgreSQL (Neon en produccion), migraciones con Flyway, pool HikariCP pequeno |
| Autenticacion | JWT (access corto + refresh rotativo), BCrypt |
| Docs de API | springdoc-openapi (`/swagger-ui.html`, `/v3/api-docs`) |
| Tests | JUnit 5, MockMvc, H2 en modo PostgreSQL |
| Build | Maven (wrapper incluido) |

> Nota sobre tests: por ahora la suite usa H2 en modo PostgreSQL con migraciones Flyway portables. Testcontainers (Postgres real) llega en fases posteriores, cuando se necesiten funciones propias de Postgres (por ejemplo `pg_trgm`).

## Estructura

Monolito modular bajo el paquete `co.edu.atlas`:

```
auth/  catalogo/  ejercicio/  media/  comentario/  metricas/  common/
```

## Variables de entorno

Copia `.env.example` a `.env` (ignorado por git) como referencia. Spring Boot lee las variables del entorno del proceso; no carga `.env` por si solo.

| Variable | Descripcion | Por defecto (dev) |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` o `prod` | `dev` |
| `PORT` | Puerto HTTP | `8080` |
| `CORS_ORIGINS` | Origenes permitidos, separados por coma | `http://localhost:5173` |
| `DATABASE_URL` | URL JDBC, p. ej. `jdbc:postgresql://<host>/<db>?sslmode=require` (Neon) | `jdbc:postgresql://localhost:5432/atlas` |
| `DB_USER` / `DB_PASSWORD` | Credenciales de la base de datos | `atlas` / `atlas` |
| `DB_POOL_SIZE` | Tamano maximo del pool Hikari | `5` |
| `JWT_SECRET` | Secreto HS256 (min. 32 caracteres). Obligatorio en prod; dev trae uno de ejemplo | solo dev |
| `FRONTEND_URL` | Base de los enlaces de correo (recuperar contrasena) | `http://localhost:5173` |
| `RESET_TTL` / `CLOSE_GRACE` | Vigencia del token de recuperacion / gracia de cierre de sesion | `30m` / `60s` |
| `RATE_LIMIT_MAX` / `RATE_LIMIT_WINDOW` | Intentos por ventana en login, registro, recuperar y restablecer | `10` / `1m` |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Seed del primer administrador (opcional, clave min. 8) | vacio |
| `SPRINGDOC_ENABLED` | Habilita Swagger UI y `/v3/api-docs` | `true` en dev, `false` en prod |
| `MEDIA_PROVIDER` | Almacenamiento de archivos: `fake` (sin red) o `r2` (Cloudflare R2) | `fake` en dev, `r2` en prod |
| `R2_ENDPOINT` / `R2_BUCKET` | Endpoint S3 de la cuenta (`https://<cuenta>.r2.cloudflarestorage.com`) y bucket | vacio |
| `R2_ACCESS_KEY_ID` / `R2_SECRET_ACCESS_KEY` | Credenciales del token de API de R2 (solo por entorno, nunca en git) | vacio |
| `R2_PUBLIC_BASE_URL` | Dominio publico del bucket (URLs de lectura estables); si falta se firman URLs de lectura temporales | vacio |
| `MEDIA_UPLOAD_TTL` / `MEDIA_READ_TTL` | Vigencia de las URLs prefirmadas de subida y lectura | `10m` / `1h` |

En el perfil `prod` no hay valores por defecto para `DATABASE_URL`, `DB_USER`, `DB_PASSWORD` ni `CORS_ORIGINS`, y la conexion exige SSL (`sslmode=require`).

## Como correr

Requisitos: JDK 21 o superior y, opcionalmente, Docker.

```bash
# 1. Base de datos local (opcional si usas Neon)
docker compose up -d

# 2. Arrancar la aplicacion (perfil dev)
./mvnw spring-boot:run

# 3. Tests y build
./mvnw verify
```

Flyway aplica las migraciones de `src/main/resources/db/migration` al arrancar, mas las especificas del motor en `db/vendor/{vendor}` (PostgreSQL crea el indice `pg_trgm` de busqueda; H2, usado solo en tests, un indice simple). Salud del servicio: `GET /actuator/health/liveness` (público, sin token, no consulta la BD) y `GET /actuator/health` (incluye la BD; para diagnóstico). Ambos responden solo `{"status":"UP"}`, sin detalles. Es el único endpoint de actuator expuesto por web.

## Pruebas de carga (k6)

Script en [`load/carga-atlas.js`](load/carga-atlas.js): 20 usuarios leyendo el atlas (con ping de sesion), comentarios y logins concurrentes durante unos 3 minutos. Requiere [k6](https://k6.io/docs/get-started/installation/) instalado.

Antes: cuentas ya registradas (`POST /auth/registro`) y al menos una ficha publicada en el entorno.

```bash
k6 run -e BASE_URL=http://localhost:8080 -e USUARIOS="correo1@ejemplo.com:clave1,correo2@ejemplo.com:clave2" load/carga-atlas.js
```

Umbrales (la prueba falla si no se cumplen): error HTTP < 1 %, p95 de lectura < 800 ms, p95 de comentarios y logins < 1 s. Son propuestas: aun no se ha ejecutado contra un entorno real (task 6.2 pendiente).

- **No apuntar a produccion**: la prueba crea comentarios reales. Usa un backend local o de prueba.
- Limites del servidor: login 10 por IP y minuto; comentar 10 por usuario y minuto. Usa entre 1 y 5 cuentas (el `setup()` hace un login por cuenta y la prueba 2 por minuto, todo desde la misma IP). Para mas cuentas sube `RATE_LIMIT_MAX` en el servidor de prueba y pasa `-e SIN_TOPE_LOGIN=1`.
- Render free: la primera peticion tras la suspension tarda; puede subir el p95.

## Autenticacion

Todo requiere `Authorization: Bearer <accessToken>` salvo `/auth/**`, `/actuator/health` y la documentacion (solo activa si `SPRINGDOC_ENABLED`).

| Endpoint | Descripcion |
|---|---|
| `POST /auth/registro` | Crea cuenta con rol de perfil (ESTUDIANTE, PROFESIONAL, PRACTICANTE, VISITANTE) |
| `POST /auth/login` | Devuelve access token (15 min) y refresh token (7 dias, guardado hasheado) |
| `POST /auth/refresh` | Rota el refresh token; reutilizar uno viejo revoca todas las sesiones |
| `POST /auth/logout` | Revoca el refresh token de inmediato |
| `POST /auth/cierre-pendiente` | Inicia la gracia de 60 s; un refresh dentro del plazo la cancela (usar `sendBeacon` al cerrar la pagina) |
| `POST /auth/recuperar` / `POST /auth/restablecer` | Recuperacion de contrasena con token de un solo uso; el correo se envia por la interfaz `CorreoSender` (en dev solo se escribe en el log) |
| `GET /auth/me` | Datos del usuario autenticado |
| `POST /admin/usuarios/{id}/promover` | Promueve a ADMIN (solo ADMIN) |

Limitaciones conocidas (riesgos aceptados): el access token ya emitido sigue valido hasta expirar (15 min) tras logout, cierre, desactivacion o cambio de rol; el rate limit es en memoria (una sola instancia) y el de login es por IP, no por cuenta; el registro responde 409 si el correo ya existe (inherente al registro inmediato, mitigado por el rate limit). Login y recuperacion no revelan si una cuenta existe: misma respuesta y tiempo similar.

## Formato de errores

Todos los errores responden con el mismo JSON:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Datos de entrada invalidos",
  "path": "/ruta",
  "fieldErrors": [{ "field": "correo", "message": "..." }]
}
```

## Despliegue

Arquitectura objetivo, usando solo servicios con plan gratuito:

| Pieza | Servicio | Notas |
|---|---|---|
| API (este repo) | **Render**, web service Docker, plan free | Se construye con el `Dockerfile` y se configura con `render.yaml` |
| Base de datos | **Neon** (PostgreSQL) | Se conecta con `sslmode=require`; el perfil `prod` exige SSL |
| Archivos (medios) | **Cloudflare R2** | Pendiente de integrar en el codigo (tarea 7.2) |
| Anti-suspension | **UptimeRobot** | Ping periodico a `/actuator/health/liveness` |

### 1. Base de datos en Neon

1. Crea un proyecto en Neon y copia el host, el nombre de la base y el usuario.
2. Arma la URL JDBC: `jdbc:postgresql://<host>/<db>?sslmode=require`.

### 2. Servicio en Render

1. En Render, *New > Blueprint* y selecciona este repositorio (usa `render.yaml`), o crea un *Web Service* con runtime **Docker** y plan **Free**.
2. Completa las variables marcadas como secretas en el panel de Render (`sync: false`):

| Variable | Valor |
|---|---|
| `DATABASE_URL` | URL JDBC de Neon (ver paso 1) |
| `DB_USER` / `DB_PASSWORD` | Credenciales de Neon |
| `JWT_SECRET` | Secreto de al menos 32 caracteres, p. ej. `openssl rand -base64 48` |
| `CORS_ORIGINS` | Origen del frontend, sin barra final (p. ej. `https://<app>.vercel.app`) |
| `FRONTEND_URL` | Mismo origen del frontend; base de los enlaces de recuperacion |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Opcionales: seed del primer administrador (clave de 8 o mas caracteres) |

`SPRING_PROFILES_ACTIVE=prod`, `SPRINGDOC_ENABLED=false` y `DB_POOL_SIZE=5` ya vienen en el blueprint. `PORT` lo inyecta Render y la aplicacion lo lee automaticamente.

Render usa `/actuator/health/liveness` como comprobacion de salud (no depende de la BD: una pausa de Neon no marca el servicio como caido). Ese endpoint es publico (no requiere token) y no expone detalles.

### 3. Verificacion

```bash
curl https://<servicio>.onrender.com/actuator/health/liveness
# {"status":"UP"}
```

### 4. Evitar la suspension del plan free

Render suspende los servicios gratuitos tras unos 15 minutos sin trafico, y Neon suspende la base por inactividad. Crea en UptimeRobot un monitor HTTP(s) sobre `https://<servicio>.onrender.com/actuator/health/liveness` con intervalo de 5 minutos (paso manual, pendiente). Mantiene despierto Render sin tocar la BD, asi Neon puede suspenderse y no gasta horas de computo; la primera consulta tras la pausa de Neon tarda algo mas.

### Limitaciones actuales

- El rate limit es en memoria: vale para una sola instancia (el plan free ya es una).
- El envio de correo aun no tiene proveedor configurado (pendiente: Resend o Brevo por HTTP); sin el, en prod la recuperacion de contrasena responde 202 pero no envia nada.
- El rate limit usa `getRemoteAddr()` con `forward-headers-strategy: native`. Hay que comprobar tras el primer despliegue que detras de Render/Cloudflare llega la IP real del cliente y no la del proxy.
- La imagen Docker aun no se ha construido ni probado con `docker build`; hay que verificarlo en el primer despliegue.

## CI

GitHub Actions (`.github/workflows/ci.yml`) ejecuta `./mvnw verify` en cada push a `main` o `develop` y en cada pull request.
