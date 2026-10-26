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
| `SPRINGDOC_ENABLED` | Habilita Swagger UI y `/v3/api-docs` | `true` en dev, `false` en prod |

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

Flyway aplica las migraciones de `src/main/resources/db/migration` al arrancar. Salud del servicio: `GET /actuator/health`.

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

## CI

GitHub Actions (`.github/workflows/ci.yml`) ejecuta `./mvnw verify` en cada push a `main` y en cada pull request.
