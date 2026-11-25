# Correcciones pendientes en `Atlas.vpp`

Checklist para alinear los diagramas de Visual Paradigm con el código del backend (`src/main/java/co/edu/atlas/`). Al terminar, exportar cada diagrama a PNG con el mismo nombre que tiene hoy en esta carpeta (en VP: clic derecho sobre el diagrama → *Export* → *Export Diagram to Image*).

Convenciones en VP:
- Nombres de parámetro reales (`ejercicioId : Long`), nunca `parameter`, `parameter2`.
- Tipo booleano siempre `boolean` (no `Bool`).
- Después de agregar miembros a una clase: clic derecho → *Fit Size* para que no se corte.

---

## 1. Vista de Desarrollo (`Vista de Desarrollo.png`) — la de más cambios

### 1.1 Paquetes
- [ ] Ponerle nombre al paquete del dominio: **Dominio** (hoy aparece sin nombre / "1").

### 1.2 Presentación — controllers que faltan
| Controller | Archivo | Operaciones |
|---|---|---|
| [ ] `CatalogoController` | `catalogo/web/CatalogoController.java` | `regiones(vista : Vista) : List<RegionResponse>`, `categoriasDeRegion(id : Long) : List<CategoriaConConteoResponse>`, `categorias() : List<CategoriaResponse>` |
| [ ] `AdminUsuarioController` | `auth/web/AdminUsuarioController.java` | `promover(id : Long) : UsuarioResponse` |
| [ ] `ComentarioAdminController` | `comentario/web/ComentarioAdminController.java` | `revision(tipo : TipoRevision, page : int, size : int) : Pagina<ComentarioRevisionResponse>`, `aceptar(id : Long) : ComentarioRevisionResponse`, `eliminar(id : Long) : void` |
| [ ] `PalabraOfensivaAdminController` | `comentario/web/PalabraOfensivaAdminController.java` | `listar(q : String, page : int, size : int) : Pagina<PalabraResponse>`, `crear(request : PalabraRequest) : PalabraResponse`, `actualizar(id : Long, request : PalabraRequest) : PalabraResponse`, `eliminar(id : Long) : void` |
| [ ] `MetricasAdminController` | `metricas/web/MetricasAdminController.java` | `resumen() : ResumenMetricasResponse`, `visitas(desde, hasta, agrupacion) : VisitasPeriodoResponse`, `masConsultados(desde, hasta, limite) : TopEjerciciosResponse`, `usuariosActivos(desde, hasta) : UsuariosActivosResponse`, `tiempoPromedio(desde, hasta) : TiempoPromedioResponse` |
| [ ] `RegistroMetricasController` | `metricas/web/RegistroMetricasController.java` | `visita(request : VisitaRequest) : void`, `ping() : SesionResponse` |

### 1.3 Presentación — corregir controllers existentes
- [ ] **MediaAdminController**
  - `solicitarSubida(ejercicioId : Long, request : SolicitudSubida) : SubidaResponse` (hoy dice `MediaReqiest`).
  - `confirmar(ejercicioId : Long, mediaId : Long) : MediaAdminResponse` (hoy `void`).
  - Agregar `eliminar(ejercicioId : Long, mediaId : Long) : void`.
- [ ] **EjercicioAdminController**
  - `crear(request : EjercicioRequest) : EjercicioAdminResponse`.
  - `publicar / archivar / restaurar(id : Long) : EjercicioAdminResponse` (hoy `void`).
  - Agregar `listar(estado, regionId, categoriaId, q, page, size) : Pagina<EjercicioAdminResumen>`, `obtener(id : Long) : EjercicioAdminResponse`, `actualizar(id : Long, request : EjercicioRequest) : EjercicioAdminResponse`, `eliminar(id : Long) : void`.
- [ ] **EjercicioAtlasController**
  - `listar(regionId : Long, categoriaId : Long) : List<EjercicioResumenResponse>` (hoy un solo `Long`).
  - `buscar(q : String) : List<EjercicioResumenResponse>`.
  - `obtener(id : Long) : EjercicioResponse` (hoy `FichaResponse`).
- [ ] **AuthController**
  - Agregar `me() : UsuarioResponse`, `registro(request : RegistroRequest) : UsuarioResponse`, `cierrePendiente(request : RefreshRequest) : void`.
  - `login(request : LoginRequest)`, `refresh(request : RefreshRequest)`, `logout(request : RefreshRequest)`, `recuperar(request : RecuperarRequest) : Map<String,String>`, `restablecer(request : RestablecerRequest) : void`.
- [ ] **ComentarioController**: `listar(ejercicioId : Long, page : int, size : int) : Pagina<ComentarioResponse>`, `crear(ejercicioId : Long, request : ComentarioRequest) : ComentarioResponse`.
- [ ] **ComentarioPropioController**: `editar(id : Long, request : ComentarioRequest) : ComentarioResponse`, `responder(id : Long, request : ComentarioRequest) : ComentarioResponse`, `eliminar(id : Long) : void`, `reportar(id : Long) : ReporteResponse`.
  - [ ] **Agregar relaciones "delega"** hacia `ComentarioService` y `ReporteComentarioService` (hoy no tiene ninguna flecha).

### 1.4 Negocio — servicios que faltan
| Servicio | Archivo | Operaciones principales | Usa |
|---|---|---|---|
| [ ] `CatalogoService` | `catalogo/service/` | `regiones(vista)`, `categorias()`, `categoriasDeRegion(regionId)` | `RegionAnatomicaRepository`, `CategoriaTerapeuticaRepository` |
| [ ] `RegistroService` | `auth/service/` | `registrar(request : RegistroRequest) : Usuario` | `UsuarioRepository` |
| [ ] `UsuarioAdminService` | `auth/admin/` | `promover(usuarioId : Long) : UsuarioResponse` | `UsuarioRepository` |
| [ ] `JwtService` | `auth/jwt/` | emitir y validar access token | — |
| [ ] `ModeracionComentarioService` | `comentario/service/` | `cola(tipo, page, size)`, `aceptar(id)`, `eliminar(id)` | `ComentarioRepository`, `ReporteComentarioRepository` |
| [ ] `PalabraOfensivaService` | `comentario/service/` | `listar(q, page, size)`, `crear(palabra)`, `actualizar(id, palabra)`, `eliminar(id)` | `PalabraOfensivaRepository` |
| [ ] `ConsultaMetricasService` | `metricas/service/` | `resumen()`, `visitasPorPeriodo(...)`, `masConsultados(...)`, `usuariosActivosPorRol(...)`, `tiempoPromedio(...)` | `VisitaRepository`, `SesionUsoRepository`, `UsuariosActivosRepository` |
| [ ] `RegistroMetricasService` | `metricas/service/` | `registrarVisita(usuarioId, tipo, ejercicioId)`, `ping(usuarioId) : SesionResponse` | `VisitaRepository`, `SesionUsoRepository` |

Conectar cada controller nuevo con su servicio con la relación **delega**.

### 1.5 Negocio — corregir servicios existentes
- [ ] **MediaService**
  - `solicitarSubida(ejercicioId : Long, req : SolicitudSubida) : SubidaResponse`.
  - `confirmar(ejercicioId : Long, mediaId : Long) : MediaAdminResponse`.
  - Agregar `eliminar(ejercicioId, mediaId) : void` y `listarConfirmados(ejercicioId) : List<MediaAdminResponse>`.
- [ ] **EjercicioAdminService**
  - Todas sus operaciones devuelven `EjercicioAdminResponse` (no `Ejercicio`) y reciben `actorId : Long`.
  - Agregar `obtener`, `actualizar`, `eliminar` y `listar`.
- [ ] **EjercicioAtlasService**
  - `listar(regionId, categoriaId) : List<EjercicioResumenResponse>`.
  - `buscar(q) : List<EjercicioResumenResponse>`.
  - `obtener(id) : EjercicioResponse`.
- [ ] **AuthService**
  - `login(request : LoginRequest) : TokenResponse` y `refrescar(refreshToken : String) : TokenResponse`.
  - Agregar `programarCierre(refreshToken : String) : void` y `perfil(usuarioId : Long) : UsuarioResponse`.
- [ ] **PasswordResetService**: `solicitar(correo : String) : void`, `restablecer(token : String, nuevaPassword : String) : void`.
- [ ] **ComentarioService**
  - `crear(ejercicioId, texto, actorId) : ComentarioResponse` y `responder(comentarioId, texto, actorId) : ComentarioResponse`.
  - Agregar `editar(id, texto, actorId)`, `eliminar(id, actorId)` y `listar(ejercicioId, viewerId, page, size)`.
- [ ] **ReporteComentarioService**: `reportar(comentarioId : Long, actorId : Long) : ReporteResponse`.
- [ ] **FiltroOfensivoService**: agregar `estadoPara(texto : String) : EstadoComentario`.

### 1.6 Datos e integración — repositorios que faltan
- [ ] `RefreshTokenRepository`, `PasswordResetTokenRepository` (`auth/token/`)
- [ ] `RegionAnatomicaRepository`, `CategoriaTerapeuticaRepository` (`catalogo/`)
- [ ] `PalabraOfensivaRepository` (`comentario/filtro/`)
- [ ] `VisitaRepository`, `SesionUsoRepository`, `UsuariosActivosRepository` (`metricas/`)

### 1.7 Relaciones que faltan
- [ ] `AuthService` → `RefreshTokenRepository` ("guarda y consulta").
- [ ] `PasswordResetService` → `UsuarioRepository` y `PasswordResetTokenRepository`.
- [ ] `FiltroOfensivoService` → `PalabraOfensivaRepository` ("consulta").
- [ ] Realizaciones (línea punteada con triángulo):
  - `AlmacenamientoR2` y `AlmacenamientoFake` → `AlmacenamientoMedia`.
  - `BrevoCorreoSender`, `LogCorreoSender` y `NoConfiguradoCorreoSender` → `CorreoSender`.
- [ ] Reemplazar todos los `parameter` / `parameter2` por el nombre real (por ejemplo `CorreoSender.enviar(destino : String, asunto : String, cuerpo : String)`, `ReporteComentarioRepository.existsByComentarioIdAndUsuarioId(comentarioId : Long, usuarioId : Long) : boolean`).

---

## 2. Vista Lógica de Clases (`Vista logica de Clases.png`)

- [ ] **Ejercicio**
  - Agregar los atributos `nombreBusqueda : String`, `creadoPor : Long`, `actualizadoPor : Long`, `createdAt : Instant`, `updatedAt : Instant` y `version : long`.
  - Agregar los métodos `actualizar(c : ContenidoFicha, actor : Long)`, `contenido() : ContenidoFicha`, `registrarEditor(actor : Long)` y `estaEliminado() : boolean`.
  - Cambiar firmas: `publicar(ahora : Instant)`, `archivar(ahora : Instant)`, `eliminar(ahora : Instant)`.
- [ ] **Comentario**
  - Agregar `esDe(usuarioId : Long) : boolean`, `visiblePara(usuarioId : Long) : boolean` y `estaEliminado() : boolean`.
  - Cambiar firmas: `editar(nuevoTexto : String, nuevoEstado : EstadoComentario, ahora : Instant)`, `eliminar(ahora : Instant)`.
- [ ] **Usuario**: agregar `normalizar(correo : String) : String` (estático, subrayado).
- [ ] **RegionAnatomica** y **CategoriaTerapeutica**: agregar `orden : int` + `getOrden() : int`.
- [ ] **PalabraOfensiva**: agregar `palabraNormalizada : String`, `createdAt : Instant`.
- [ ] **Media**: agregar `createdAt : Instant`, `estaConfirmado() : boolean`.
- [ ] **Visita**: agregar `createdAt : Instant`.
- [ ] **Dosificacion**: agregar `vacia() : Dosificacion` (estático).
- [ ] **SesionUso**
  - `vigente(ahora : Instant, inactividad : Duration) : boolean`.
  - `registrarPing(ahora : Instant)`.
- [ ] **RefreshToken**: `haExpirado(ahora : Instant) : boolean`, `programarCierre(hasta : Instant)`.
- [ ] **PasswordResetToken**: `esUtilizable(ahora : Instant) : boolean`.
- [ ] Cambiar todos los `Bool` por `boolean`.
- [ ] Enums `TipoRevision` y `Agrupacion`: o se conectan (dependencia desde `Comentario`/`Visita`) o se quitan de esta vista (son parámetros de consulta, no dominio).

---

## 3. Vista Funcional de Componentes (`Vista Funcional Componentes.png`)

- [ ] **Multimedia**: agregar la interfaz provista "Eliminar multimedia".
- [ ] **Fichas de ejercicios**: agregar "Listar fichas (admin)" y "Obtener ficha (admin)".
- [ ] **Usuarios**: agregar "Consultar perfil".
- [ ] **Catálogo anatómico**: agregar "Listar categorías de una región".
- [ ] **Filtro de contenido**: agregar "Editar palabra ofensiva".
- [ ] "Enviar email" y "Almacenar archivo (Cloudflare R2)": dibujar como componentes externos `<<external>>` **Brevo** y **Cloudflare R2** que proveen la interfaz.
- [ ] Corregir codificación: "Caché sin conexión (frontend PWA)" y la etiqueta "Guardar ficha en caché (ETag HTTP)" que sale cortada.
- [ ] Reordenar las líneas que se cruzan, sobre todo la de "Recuperar contraseña".

---

## 4. Vista de Contexto (`Vista de Contexto.png`)

- [ ] Separar y alargar las etiquetas de los flujos con *Almacenamiento de archivos*:
  - Sistema → Almacenamiento: "Verificación y borrado de archivos".
  - Almacenamiento → Sistema: "Metadatos del archivo".
  - Administrador → Almacenamiento: "Archivos (videos e imágenes) por URL prefirmada".
  - Almacenamiento → Usuario: "Videos e imágenes".
- [ ] Corregir tildes (codificación UTF-8 en las etiquetas).

---

## 5. Vista de Despliegue (`Vista Despliegue.png`)

- [ ] Conectar la ruta **atlas-cinesiterapia-backend.jar → Cloudflare R2** (hoy la etiqueta "HTTPS 443 (API S3: verificar y borrar)" queda colgando).
- [ ] (Opcional) Dentro de *Navegador*, agregar el artefacto **Service Worker / Cache Storage** (modo sin conexión de la PWA).

---

## 6. Al terminar
- [ ] Guardar `Atlas.vpp`.
- [ ] Exportar los 5 diagramas a PNG sobrescribiendo los de esta carpeta.
- [ ] Commit: `docs(diagramas): align VP views with backend code`.
