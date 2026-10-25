# Contrato de la API — Atlas Interactivo de Cinesiterapia

Documento de referencia para el frontend. Se mantiene actualizado con cada task.

Leyenda de estado: **[implementado]** ya existe en el backend; **[planificado]** definido aqui y todavia no disponible (se marca como implementado en el commit de su task).

- Base URL local: `http://localhost:8080` (sin prefijo `/api`).
- Formato: JSON UTF-8 (`Content-Type: application/json`). Fechas en ISO-8601 UTC (`2026-10-25T14:30:00Z`).
- Nombres de campos en espanol y `camelCase`. Enumeraciones en `MAYUSCULAS_CON_GUION_BAJO`.
- Swagger UI (solo dev): `/swagger-ui.html`, esquema en `/v3/api-docs`.

## Estado de implementacion por endpoint

| Endpoint | Task | Estado |
|---|---|---|
| `/auth/*`, `/admin/usuarios/{id}/promover` | 1.x | implementado |
| `POST /admin/ejercicios`, `GET /admin/ejercicios`, `GET /admin/ejercicios/{id}`, `PUT /admin/ejercicios/{id}` (validacion minima) | 2.3 | implementado |
| Subida de media (`/admin/ejercicios/{id}/media/...`) y campo `media` en `EjercicioAdmin` | 2.4 | implementado |
| `POST /admin/ejercicios/{id}/publicar`, campo `camposFaltantes` | 2.5 | implementado |
| Validacion completa de `PUT` en fichas publicadas/archivadas | 2.6 | implementado |
| `archivar`, `restaurar`, `DELETE` | 2.7 | implementado |
| `GET /regiones`, `/regiones/{id}/categorias`, `/categorias` | 3.1 | implementado |
| `GET /ejercicios?region&categoria` | 3.2 | implementado |
| `GET /ejercicios/{id}` | 3.3 | implementado |
| `GET /ejercicios/buscar?q=` | 3.4 | implementado |
| Cabeceras de cache (ETag / Cache-Control) | 3.5 | implementado |
| `GET/POST /ejercicios/{id}/comentarios` | 4.2 | implementado |
| `PUT/DELETE /comentarios/{id}` (propio) | 4.3 | implementado |
| `POST /comentarios/{id}/respuestas` | 4.4 | implementado |
| Filtro de ofensivas (estado `EN_REVISION`, campo `enRevision`) | 4.5 | implementado |

Los datos semilla (27 regiones y 9 categorias, seccion 3) ya existen en la base desde la task 2.1.

---

## 1. Convenciones generales

### Autenticacion

Todo requiere `Authorization: Bearer <accessToken>` salvo `/auth/**`, `/actuator/health` y la documentacion.

- Access token: JWT, vigencia 15 min (`expiresIn` en segundos).
- Refresh token: opaco, vigencia 7 dias, **rotativo** (cada `/auth/refresh` entrega uno nuevo y revoca el anterior).
- Roles de sistema: `USER` y `ADMIN`. Los endpoints `/admin/**` exigen `ADMIN` (403 `ACCESS_DENIED` si no).
- Roles de perfil (elegidos al registrarse): `ESTUDIANTE`, `PROFESIONAL`, `PRACTICANTE`, `VISITANTE`.

### Formato de error (uniforme)

```json
{
  "timestamp": "2026-10-25T14:30:00.123Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Datos de entrada invalidos",
  "path": "/auth/registro",
  "fieldErrors": [ { "field": "correo", "message": "Correo invalido" } ]
}
```

`fieldErrors` solo aparece cuando hay detalle por campo (se omite si esta vacio). El front debe ramificar por `code` (estable), no por `message`.

Codigos genericos:

| HTTP | code | Cuando |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Falla de validacion del cuerpo/parametros; detalle en `fieldErrors` |
| 400 | `MALFORMED_REQUEST` | JSON ilegible, parametro faltante o de tipo incorrecto |
| 401 | `NO_AUTENTICADO` | Falta token, es invalido o expiro el access token |
| 403 | `ACCESS_DENIED` | Rol insuficiente |
| 404 | `NOT_FOUND` | Ruta inexistente |
| 405 | `METHOD_NOT_ALLOWED` | Metodo no soportado |
| 429 | `DEMASIADOS_INTENTOS` | Rate limit (cabecera `Retry-After` en segundos) |
| 500 | `INTERNAL_ERROR` | Error no controlado |

### Paginacion (solo listados de administracion)

Query: `page` (desde 0, por defecto 0) y `size` (1–100, por defecto 20). Respuesta:

```json
{ "content": [ ... ], "page": 0, "size": 20, "totalElements": 57, "totalPages": 3 }
```

Los listados del atlas (regiones, categorias, ejercicios por filtro, busqueda) devuelven un **arreglo simple** (volumen pequeno).

### Cache (fase 3, ver 7)

Los `GET` del atlas incluyen `ETag` y `Cache-Control`; reenviar `If-None-Match` para recibir `304 Not Modified`.

---

## 2. Autenticacion [implementado]

### `POST /auth/registro` — 201

Publico. Rate limit.

```json
// request
{ "correo": "ana@correo.com", "password": "minimo8caracteres", "rolPerfil": "ESTUDIANTE" }
```
- `correo`: obligatorio, formato correo, max 255 (se guarda en minusculas).
- `password`: obligatorio, 8–72 caracteres.
- `rolPerfil`: obligatorio, uno de los 4 roles de perfil.

```json
// response 201  (UsuarioResponse)
{ "id": 7, "correo": "ana@correo.com", "rolPerfil": "ESTUDIANTE", "rolSistema": "USER" }
```
Errores: 400 `VALIDATION_ERROR`; 409 `CORREO_EN_USO`; 429 `DEMASIADOS_INTENTOS`. No inicia sesion: hay que llamar a `/auth/login` despues.

### `POST /auth/login` — 200

Publico. Rate limit.

```json
// request
{ "correo": "ana@correo.com", "password": "..." }
// response 200  (TokenResponse)
{
  "accessToken": "eyJ...",
  "refreshToken": "q3Vx...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "usuario": { "id": 7, "correo": "ana@correo.com", "rolPerfil": "ESTUDIANTE", "rolSistema": "USER" }
}
```
Errores: 400 `VALIDATION_ERROR` (campos vacios); 401 `CREDENCIALES_INVALIDAS` (correo inexistente, clave incorrecta o cuenta inactiva; mensaje identico para no revelar cual); 429.

### `POST /auth/refresh` — 200

Publico. Rota el refresh token y devuelve un `TokenResponse` completo (guardar el nuevo `refreshToken`; el anterior queda revocado).

```json
{ "refreshToken": "q3Vx..." }
```
Errores: 401 `REFRESH_INVALIDO` (desconocido, revocado, expirado o usuario inactivo; reutilizar uno ya rotado revoca **todas** las sesiones del usuario); 401 `SESION_EXPIRADA` (vencio la gracia de `/auth/cierre-pendiente`).

### `POST /auth/logout` — 204

Publico. Revoca el refresh token. Cuerpo `{ "refreshToken": "..." }`. Idempotente: un token desconocido tambien responde 204.

### `POST /auth/cierre-pendiente` — 204

Publico. Inicia el periodo de gracia de 60 s (HU-19). Cuerpo `{ "refreshToken": "..." }`. Si dentro de esos 60 s se llama a `/auth/refresh` con ese token, la sesion continua (gracia cancelada porque el refresh rota el token); si no, el siguiente refresh responde 401 `SESION_EXPIRADA`. Pensado para `navigator.sendBeacon` al cerrar la pestana (enviar como `Blob` con `type: 'application/json'`). Siempre 204.

### `POST /auth/recuperar` — 202

Publico. Rate limit. Cuerpo `{ "correo": "ana@correo.com" }`. Respuesta siempre igual exista o no el correo:

```json
{ "mensaje": "Si el correo esta registrado, recibiras un enlace para restablecer tu contrasena" }
```
El correo contiene un enlace `FRONTEND_URL/...?token=<token>` (un solo uso, vigencia 30 min). Errores: 400 `VALIDATION_ERROR`; 429.

### `POST /auth/restablecer` — 204

Publico. Rate limit.

```json
{ "token": "<token del enlace>", "nuevaPassword": "otraclave123" }
```
`nuevaPassword`: 8–72 caracteres. Errores: 400 `TOKEN_INVALIDO` (inexistente, usado o expirado); 400 `VALIDATION_ERROR`; 429.

### `GET /auth/me` — 200

Requiere token. Devuelve `UsuarioResponse`. 401 `NO_AUTENTICADO` si no hay token valido.

### `POST /admin/usuarios/{id}/promover` — 200 (solo ADMIN)

Devuelve `UsuarioResponse` con `rolSistema: "ADMIN"`. Idempotente. Errores: 403 `ACCESS_DENIED`; 404 `USUARIO_NO_ENCONTRADO`.

---

## 3. Catalogos [implementado, tasks 2.1 y 3.1]

Requieren sesion (cualquier rol). Los IDs son estables (seed); el front debe mapear por `svgId`.

### Tipos

```ts
type Vista = 'ANTERIOR' | 'POSTERIOR';
interface Region    { id: number; nombre: string; vista: Vista; svgId: string; orden: number }
interface Categoria { id: number; nombre: string; orden: number }
```

### `GET /regiones` — 200
Query opcional `vista=ANTERIOR|POSTERIOR`. Devuelve `Region[]` ordenado por `vista`, `orden`. Sin distincion izquierda/derecha.

### `GET /regiones/{id}/categorias` — 200
Devuelve `CategoriaConConteo[]` (las 9 categorias, ordenadas por `orden`):

```json
[ { "id": 1, "nombre": "Movilización pasiva", "orden": 1, "totalEjercicios": 3 } ]
```
`totalEjercicios` = ejercicios **publicados** de esa region en esa categoria (0 = estado vacio). Errores: 404 `REGION_NO_ENCONTRADA`.

`GET /regiones` con `vista` desconocida (por ejemplo `LATERAL`): 400 `MALFORMED_REQUEST`.

### `GET /categorias` — 200
`Categoria[]` (las 9). Util para formularios de administracion.

### Seed de categorias (id → nombre)
1 Movilización pasiva · 2 Movilización activo-asistida · 3 Movilización activa libre · 4 Ejercicio activo resistido · 5 Estiramiento y elongación · 6 Fortalecimiento isométrico · 7 Propiocepción y equilibrio · 8 Control motor y estabilización · 9 Facilitación neuromuscular propioceptiva (FNP)

### Seed de regiones (`svgId`, con prefijo de vista)

Vista `ANTERIOR`: `anterior-cabeza-cuello`, `anterior-hombro`, `anterior-brazo`, `anterior-codo`, `anterior-antebrazo`, `anterior-muneca-mano`, `anterior-torax`, `anterior-abdomen`, `anterior-cadera`, `anterior-muslo`, `anterior-rodilla`, `anterior-pierna`, `anterior-tobillo-pie` (13).

Vista `POSTERIOR`: `posterior-cabeza-cuello`, `posterior-cervical`, `posterior-hombro-escapula`, `posterior-brazo`, `posterior-codo`, `posterior-antebrazo`, `posterior-muneca-mano`, `posterior-dorsal`, `posterior-lumbar`, `posterior-gluteo`, `posterior-muslo`, `posterior-rodilla`, `posterior-pierna`, `posterior-tobillo-pie` (14). En total 27 regiones.

---

## 4. Consulta del atlas [planificado, tasks 3.2–3.4]

Requieren sesion (cualquier rol). Solo exponen fichas **PUBLICADO**; borrador, archivada o eliminada responde como inexistente.

### Tipos

```ts
interface EjercicioResumen {
  id: number; nombre: string;
  region:    { id: number; nombre: string; svgId: string };
  categoria: { id: number; nombre: string };
}

interface Media {
  id: number;
  tipo: 'VIDEO' | 'IMAGEN';
  rol: 'POSICION_INICIAL' | 'POSICION_FINAL' | 'TOMA_TERAPEUTA' | 'OTRO' | null; // null en video
  contentType: string;
  tamanoBytes: number;
  url: string;        // URL de lectura (puede ser temporal; usarla tal cual)
  orden: number;
}

interface Dosificacion {
  series: string; repeticiones: string; intensidad: string;
  tiempo: string; descanso: string; frecuencia: string;   // texto libre: "3x12", "hasta fatiga"
}

interface Ejercicio {                 // ficha completa (HU-02)
  id: number; nombre: string;
  region:    { id: number; nombre: string; vista: Vista; svgId: string };
  categoria: { id: number; nombre: string };
  objetivoTerapeutico: string;
  estructurasIntervenidas: string;
  indicaciones: string;
  precaucionesContraindicaciones: string;
  posicionPaciente: string;
  posicionFisioterapeuta: string;
  ejecucion: string;
  comandosVerbales: string;
  dosificacion: Dosificacion;
  progresion: string | null;
  erroresComunes: string | null;
  estudiantesCreadores: string[];
  cohorte: string;                    // "AAAA-N", p. ej. "2025-1"
  profesoraRevisora: string;
  video: Media | null;
  imagenes: Media[];                  // ordenadas por orden
  actualizadoEn: string;
}
```

### `GET /ejercicios?region={id}&categoria={id}` — 200
`EjercicioResumen[]` publicados, ordenados por nombre (`region` incluye tambien `vista`). Ambos filtros son opcionales y combinables; sin filtros devuelve todos. Lista vacia `[]` = estado vacio (HU-01). 400 `MALFORMED_REQUEST` si un id no es numerico.

### `GET /ejercicios/{id}` — 200
`Ejercicio`. 404 `EJERCICIO_NO_ENCONTRADO` si no existe o no esta publicado. Cabecera `ETag` (ver 7).

### `GET /ejercicios/buscar?q=texto` — 200
`EjercicioResumen[]` publicados cuyo nombre contiene `q` (parcial, sin distinguir mayusculas ni tildes: `"flexion"` encuentra `"Flexión"`), maximo 50 resultados, ordenados por nombre. `q` vacio, solo espacios o de mas de 100 caracteres: 400 `VALIDATION_ERROR` (si el parametro `q` ni se envia: 400 `MALFORMED_REQUEST`); el front no deberia llamar con texto vacio. Sin coincidencias: `[]`.

---

## 5. Administracion de fichas [planificado, tasks 2.2–2.8]

Todo `/admin/ejercicios/**` exige `ADMIN`. **Todos los administradores ven y editan todas las fichas** (HU-15); `creadoPor` es informativo.

### Estados

`BORRADOR` → `PUBLICADO` ⇄ `ARCHIVADO`. Las fichas eliminadas (borrado blando) desaparecen de toda la API.

### Tipos

```ts
interface EjercicioRequest {                 // cuerpo de POST y PUT
  nombre: string;                            // obligatorio, max 200
  regionId: number;                          // obligatorio
  categoriaId: number;                       // obligatorio
  objetivoTerapeutico?: string | null;       // texto, max 5000 cada campo de texto
  estructurasIntervenidas?: string | null;
  indicaciones?: string | null;
  precaucionesContraindicaciones?: string | null;
  posicionPaciente?: string | null;
  posicionFisioterapeuta?: string | null;
  ejecucion?: string | null;
  comandosVerbales?: string | null;
  dosificacion?: Partial<Dosificacion> | null;   // cada campo max 100
  progresion?: string | null;
  erroresComunes?: string | null;
  estudiantesCreadores?: string[] | null;    // max 20 items, cada uno max 150
  cohorte?: string | null;                   // regex ^\d{4}-\d{1,2}$ ("2025-1")
  profesoraRevisora?: string | null;         // max 150
}

interface EjercicioAdmin {                   // respuesta de toda operacion admin sobre una ficha
  id: number;
  estado: 'BORRADOR' | 'PUBLICADO' | 'ARCHIVADO';
  // ...mismos campos de contenido que Ejercicio (con null/[] si faltan; region y categoria como objetos)
  media: MediaAdmin[];                       // todas las confirmadas, video primero
  creadoPor:       { id: number; correo: string };
  actualizadoPor:  { id: number; correo: string } | null;
  creadoEn: string; actualizadoEn: string;
  publicadoEn: string | null; archivadoEn: string | null;
  camposFaltantes: string[];                 // lo que falta para poder publicar ([] = lista para publicar)
  version: number;
}
interface MediaAdmin extends Media { estado: 'CONFIRMADO' | 'PENDIENTE' }

interface EjercicioAdminResumen {
  id: number; nombre: string; estado: string;
  region: { id: number; nombre: string; svgId: string };
  categoria: { id: number; nombre: string };
  creadoPor: { id: number; correo: string };
  actualizadoEn: string;
}
```

Los textos se recortan (`trim`) y las cadenas vacias se guardan como `null`.

### `POST /admin/ejercicios` — 201
Cuerpo `EjercicioRequest`. Crea la ficha en `BORRADOR` con validacion minima (`nombre`, `regionId`, `categoriaId`; el resto opcional, HU-10). Devuelve `EjercicioAdmin`.
Errores: 400 `VALIDATION_ERROR`; 400 `REGION_INVALIDA` / `CATEGORIA_INVALIDA` (id inexistente).

### `GET /admin/ejercicios` — 200
Query opcionales: `estado`, `regionId`, `categoriaId`, `q` (nombre, sin tildes), `page`, `size`. Devuelve `Page<EjercicioAdminResumen>` ordenada por `actualizadoEn` descendente. Excluye eliminadas. Con `estado=ARCHIVADO` es la vista de archivadas (HU-14).

### `GET /admin/ejercicios/{id}` — 200
`EjercicioAdmin`. 404 `EJERCICIO_NO_ENCONTRADO`.

### `PUT /admin/ejercicios/{id}` — 200 [implementado]
Reemplaza el contenido completo (los campos ausentes o `null` se vacian). Cuerpo `EjercicioRequest`. Devuelve `EjercicioAdmin`.
- `BORRADOR`: validacion minima.
- `PUBLICADO` / `ARCHIVADO`: ademas exige todos los campos obligatorios para publicar (HU-12); si falta alguno, 422 `FICHA_INCOMPLETA` y no se guarda. La ficha publicada sigue visible durante la edicion.
Errores: 400 `VALIDATION_ERROR`, `REGION_INVALIDA`, `CATEGORIA_INVALIDA`; 404; 422 `FICHA_INCOMPLETA`.

### `POST /admin/ejercicios/{id}/publicar` — 200 [implementado]
Sin cuerpo. Pasa `BORRADOR` → `PUBLICADO`. Devuelve `EjercicioAdmin`.
Errores: 404; 409 `ESTADO_INVALIDO` (solo se publica desde BORRADOR; se evalua antes que la completitud); 422 `FICHA_INCOMPLETA` (`fieldErrors` en el orden de la lista de abajo; `camposFaltantes` del `EjercicioAdmin` trae los mismos nombres para deshabilitar el boton Publicar):

```json
{
  "status": 422, "code": "FICHA_INCOMPLETA", "message": "La ficha tiene campos obligatorios pendientes",
  "fieldErrors": [
    { "field": "objetivoTerapeutico", "message": "Campo obligatorio" },
    { "field": "dosificacion.series", "message": "Campo obligatorio" },
    { "field": "media", "message": "Agrega al menos una imagen o un video" }
  ]
}
```

**Campos obligatorios para publicar:** `nombre`, `regionId`, `categoriaId`, `objetivoTerapeutico`, `estructurasIntervenidas`, `indicaciones`, `precaucionesContraindicaciones`, `posicionPaciente`, `posicionFisioterapeuta`, `ejecucion`, `comandosVerbales`, `dosificacion.{series,repeticiones,intensidad,tiempo,descanso,frecuencia}`, `media` (al menos 1 imagen o 1 video confirmado), `estudiantesCreadores` (al menos 1), `cohorte`, `profesoraRevisora`. Opcionales: `progresion`, `erroresComunes`. (Los `field` de `fieldErrors` usan estos nombres.)

### `POST /admin/ejercicios/{id}/archivar` — 200 [implementado]
`PUBLICADO` → `ARCHIVADO`. Devuelve `EjercicioAdmin`. 409 `ESTADO_INVALIDO` si no esta publicada.

### `POST /admin/ejercicios/{id}/restaurar` — 200 [implementado]
`ARCHIVADO` → `PUBLICADO`. Devuelve `EjercicioAdmin`. 409 `ESTADO_INVALIDO` si no esta archivada.

### `DELETE /admin/ejercicios/{id}` — 204 [implementado]
Borrado blando (`deleted_at`) desde cualquier estado; deja de aparecer en toda la API. El front debe pedir confirmacion (HU-13). 404 si ya estaba eliminada.

---

## 6. Multimedia [implementado, task 2.4]

Los archivos **no pasan por el backend**: se suben directo a Cloudflare R2 con una URL prefirmada (flujo de 3 pasos).

Reglas (el backend las valida en el paso 1, y R2 las hace cumplir con la firma):

| Tipo | `contentType` permitidos | Tamano max | Cantidad por ficha |
|---|---|---|---|
| `VIDEO` | `video/mp4`, `video/webm`, `image/gif` | 25 MB (26 214 400 bytes) | 1 |
| `IMAGEN` | `image/jpeg`, `image/png`, `image/webp` | 5 MB (5 242 880 bytes) | 8 |

### Paso 1 — `POST /admin/ejercicios/{id}/media/subida` — 201

```json
// request
{
  "tipo": "IMAGEN",                    // VIDEO | IMAGEN
  "rol": "POSICION_INICIAL",           // POSICION_INICIAL | POSICION_FINAL | TOMA_TERAPEUTA | OTRO; obligatorio en IMAGEN, se ignora en VIDEO
  "nombreArchivo": "inicio.png",       // informativo, max 200
  "contentType": "image/png",
  "tamanoBytes": 183421
}
// response 201
{
  "mediaId": 12,
  "urlSubida": "https://<cuenta>.r2.cloudflarestorage.com/<bucket>/ejercicios/5/....png?X-Amz-...",
  "metodo": "PUT",
  "cabeceras": { "Content-Type": "image/png" },
  "expiraEn": "2026-10-25T14:40:00Z"   // la URL vive 10 min
}
```
Errores: 404 `EJERCICIO_NO_ENCONTRADO`; 400 `VALIDATION_ERROR`; 400 `MEDIA_TIPO_NO_SOPORTADO` (el mensaje lista los formatos admitidos); 400 `MEDIA_DEMASIADO_GRANDE`; 400 `MEDIA_ROL_REQUERIDO` (imagen sin `rol`); 409 `MEDIA_LIMITE_ALCANZADO` (las subidas pendientes recientes, de menos de 10 min, tambien cuentan).

### Paso 2 — subir el archivo a R2
`PUT {urlSubida}` con el archivo crudo como cuerpo y **exactamente** las `cabeceras` indicadas (el `Content-Length` real debe coincidir con `tamanoBytes`). Sin `Authorization` de la API. Si falla la firma o vence la URL, pedir una subida nueva. El bucket de R2 debe tener CORS que permita `PUT` desde el origen del front con la cabecera `Content-Type`.

> En desarrollo (`MEDIA_PROVIDER=fake`, por defecto) `urlSubida` apunta a un dominio ficticio: el front puede **omitir el paso 2** y confirmar directamente; el backend da por subido el archivo.

### Paso 3 — `POST /admin/ejercicios/{id}/media/{mediaId}/confirmar` — 200
Sin cuerpo. El backend verifica que el objeto exista en el almacenamiento y marca el medio `CONFIRMADO`; solo entonces cuenta para publicar y aparece en la ficha. Devuelve `MediaAdmin`. Errores: 404 `MEDIA_NO_ENCONTRADA`; 409 `MEDIA_NO_SUBIDA` (el archivo no esta en el almacenamiento); 409 `MEDIA_TAMANO_INVALIDO` (el tamano real no coincide con el declarado: el medio se descarta y hay que pedir una subida nueva). Confirmar un medio ya confirmado devuelve 200 igual.

### `DELETE /admin/ejercicios/{id}/media/{mediaId}` — 204
Elimina el medio y su objeto. Errores: 404 `MEDIA_NO_ENCONTRADA`; 409 `MEDIA_REQUERIDA` (no se puede quitar el ultimo medio de una ficha publicada o archivada).

Los medios en estado pendiente que nunca se confirman no se muestran ni cuentan.

---

## 7. Cache para modo offline [implementado, task 3.5]

- Catalogos (`/regiones`, `/regiones/{id}/categorias`, `/categorias`): `Cache-Control: private, max-age=3600` + `ETag`.
- Atlas (`/ejercicios`, `/ejercicios/{id}`, `/ejercicios/buscar`): `Cache-Control: private, max-age=0, must-revalidate` + `ETag`. El navegador/service worker revalida con `If-None-Match` y recibe `304` si no cambio.
- `/admin/**` y `/auth/**`: nunca se cachean (`no-store`). Las respuestas de error tampoco (`no-store`, sin ETag).
- El `ETag` es debil y se calcula sobre el cuerpo: si cambia cualquier dato de la respuesta (incluida una `url` de media prefirmada y temporal) cambia el ETag. Con `R2_PUBLIC_BASE_URL` configurado las `url` son estables y el `304` funciona tambien en `GET /ejercicios/{id}`; sin el, la ficha rara vez devolvera 304 (las imagenes ya cargadas si pueden seguir cacheadas por su propia URL).
- Todas las respuestas cacheables llevan `Vary: Authorization`.

---

## 8. Comentarios [en construccion, fase 4]

Disponible para cualquier usuario autenticado. Los comentarios son **texto plano**: el front debe renderizarlos como texto (nunca como HTML). El backend elimina caracteres de control e invisibles y rechaza cualquier etiqueta HTML (`TEXTO_INVALIDO`).

### Tipos

```ts
type EstadoComentario = "PUBLICADO" | "EN_REVISION" | "ELIMINADO";

type Autor = { id: number; nombre: string; rolPerfil: RolPerfil }; // nombre = parte local del correo; nunca el correo completo

type Comentario = {
  id: number;
  ejercicioId: number;
  padreId: number | null;      // null en comentarios raiz
  autor: Autor | null;         // null si estado = ELIMINADO
  texto: string | null;        // null si estado = ELIMINADO (mostrar "comentario eliminado")
  estado: EstadoComentario;
  enRevision: boolean;         // true solo para el propio autor (mostrar atenuado, "esperando revision")
  eliminado: boolean;
  propio: boolean;             // true si el usuario autenticado es el autor (mostrar Editar / Eliminar)
  createdAt: string;
  editadoAt: string | null;
  respuestas: Comentario[];    // solo en raices (vacio en respuestas); orden cronologico
};
```

Hilos de un nivel: una respuesta siempre cuelga de un comentario raiz. Un comentario `EN_REVISION` solo lo ve su autor; los demas usuarios nunca lo reciben. Un comentario raiz `ELIMINADO` solo aparece (sin texto ni autor) mientras conserve respuestas visibles.

### `GET /ejercicios/{ejercicioId}/comentarios?page=0&size=20` — 200 [implementado]

Pagina (`Pagina<Comentario>`, ver Paginacion; `size` 1–100) de comentarios **raiz**, del mas reciente al mas antiguo, cada uno con sus `respuestas` visibles para quien consulta. Pagina vacia si no hay comentarios (mostrar "aun no hay comentarios"). Respuestas sin cache (`no-store`: dependen del usuario).
Errores: 404 `EJERCICIO_NO_ENCONTRADO` (ficha inexistente o no publicada).

### `POST /ejercicios/{ejercicioId}/comentarios` — 201 [implementado]

Cuerpo: `{ "texto": string }` (1–1000 caracteres tras limpiar). Devuelve `Comentario`.
Errores: 400 `VALIDATION_ERROR` (vacio / mas de 1000), 400 `TEXTO_INVALIDO` (contiene HTML o queda vacio), 404 `EJERCICIO_NO_ENCONTRADO`, 429 `DEMASIADOS_INTENTOS` (limite por usuario contra spam; ver `RATE_LIMIT_MAX`/`RATE_LIMIT_WINDOW`).

### `PUT /comentarios/{id}` — 200 [implementado]

Edita el comentario propio. Cuerpo: `{ "texto": string }` (mismas reglas que al crear). Devuelve `Comentario` con `editadoAt` y `respuestas: []` (volver a pedir el listado si se necesitan las respuestas).
Errores: 400 `VALIDATION_ERROR` / `TEXTO_INVALIDO`, 403 `NO_ES_AUTOR` (comentario visible de otra persona; los administradores tampoco editan texto ajeno), 404 `COMENTARIO_NO_ENCONTRADO` (no existe, esta eliminado o es un comentario en revision de otro usuario), 429 `DEMASIADOS_INTENTOS`.

### `DELETE /comentarios/{id}` — 204 [implementado]

Borrado blando del comentario propio (el texto se vacia y se descartan sus reportes). Si tenia respuestas, el listado lo sigue devolviendo como `eliminado: true` sin texto ni autor.
Errores: 403 `NO_ES_AUTOR`, 404 `COMENTARIO_NO_ENCONTRADO`.

### `POST /comentarios/{id}/respuestas` — 201 [implementado]

Responde al comentario `{id}`. Cuerpo: `{ "texto": string }`. Devuelve la `Comentario` creada (con `padreId`). **Decision de diseno: los hilos son de un nivel y responder a una respuesta se aplana**: la nueva respuesta cuelga del comentario raiz del hilo (el front puede anteponer "@nombre" al texto si quiere). Las respuestas pasan por las mismas reglas de texto que los comentarios.
Errores: 400 `VALIDATION_ERROR` / `TEXTO_INVALIDO`, 404 `COMENTARIO_NO_ENCONTRADO` (inexistente, eliminado o en revision de otro usuario), 409 `COMENTARIO_NO_RESPONDIBLE` (el comentario o su raiz no esta publicado, p. ej. tu propio comentario en revision), 404 `EJERCICIO_NO_ENCONTRADO` (ficha ya no publicada), 429 `DEMASIADOS_INTENTOS`.

### Filtro automatico de contenido ofensivo [implementado, task 4.5]

Al crear, editar o responder, el texto se compara con una lista de palabras ofensivas guardada en la base. La comparacion ignora mayusculas y tildes, aplica sustituciones (`4`→a, `3`→e, `0`→o, `1`→i, `5`→s, `7`→t, `@`→a, `$`→s), tolera separadores entre letras (`p.u.t.a`, `p u t a`, `p-u-t-a`) y letras repetidas (`puuuuta`), y busca palabra completa (no marca "reputacion"). Si hay coincidencia el comentario queda `EN_REVISION`: la respuesta `201`/`200` ya trae `estado: "EN_REVISION"` y `enRevision: true`, y el comentario solo se devuelve a su autor (los demas no lo ven ni como contador). Una edicion limpia vuelve a `PUBLICADO`; una edicion ofensiva lo oculta de nuevo.
Mostrar al autor el comentario atenuado, con icono de advertencia y el mensaje "esperando revision" cuando `enRevision` sea true.

---

## 9. Modulos futuros

Moderacion de comentarios y metricas se documentaran aqui al implementarse.
