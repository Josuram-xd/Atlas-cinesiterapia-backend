# Resumen de historias de usuario — Atlas Interactivo de Cinesiterapia

Generado a partir de [`docs/requerimientos.md`](../requerimientos.md) con la skill
`user-stories-invest-gherkin`. Se entrega por módulos; este resumen se actualiza en
cada entrega.

**Decisiones de alcance acordadas con el usuario:**
- RF_005–RF_021 (campos de la ficha de ejercicio) se agrupan por sección lógica en
  lugar de una historia por cada RF.
- RNF_004 (uso sin conexión) asume que el acceso offline requiere haber iniciado
  sesión previamente con conexión; el contenido consultado se cachea localmente y
  queda disponible sin internet mientras la sesión siga vigente.
- Las reglas de integridad de datos (RF_022, RF_023, RF_043, RF_044, RF_053) no se
  documentan como historias propias: se incorporan como criterios de aceptación de
  la historia funcional a la que pertenecen.

## Módulo 1 — Navegación y consulta del atlas

| ID | Título | Requerimientos cubiertos | Valor de negocio |
|---|---|---|---|
| [HU-01](HU-01-explorar-atlas-por-region-y-categoria.md) | Explorar el atlas por región anatómica y categoría terapéutica | RF_001, RF_002, RF_003 | Permite al usuario llegar al listado de ejercicios relevantes navegando por el cuerpo humano |
| [HU-02](HU-02-ver-ficha-completa-de-ejercicio.md) | Ver la ficha completa de un ejercicio | RF_004 | Entrega el detalle clínico completo del ejercicio, el valor central del atlas |
| [HU-03](HU-03-buscar-ejercicios-por-nombre.md) | Buscar ejercicios por nombre | RF_051 | Acceso directo a un ejercicio conocido sin navegar por región/categoría |

## Módulo 2 — Gestión de la ficha de ejercicio

Los campos de RF_005–RF_021 se agruparon por sección lógica en 6 historias (ver
decisión de alcance arriba); RF_022 y RF_023 quedan como criterios de aceptación
dentro de HU-04.

| ID | Título | Requerimientos cubiertos | Valor de negocio |
|---|---|---|---|
| [HU-04](HU-04-informacion-general-de-la-ficha.md) | Registrar la información general de la ficha | RF_005, RF_022, RF_023 | Crea el registro base de la ficha sobre el que se apoya el resto del contenido |
| [HU-05](HU-05-protocolo-clinico-del-ejercicio.md) | Registrar el protocolo clínico del ejercicio | RF_006, RF_007, RF_008, RF_009 | Documenta el criterio clínico que justifica el uso del ejercicio |
| [HU-06](HU-06-ejecucion-del-ejercicio.md) | Registrar la ejecución del ejercicio | RF_010, RF_011, RF_012, RF_013 | Guía operativa para reproducir correctamente el ejercicio |
| [HU-07](HU-07-dosificacion-y-progresion.md) | Registrar dosificación, progresión y errores comunes | RF_014, RF_015, RF_016 | Permite prescribir el ejercicio de forma segura y ajustarlo/corregirlo |
| [HU-08](HU-08-multimedia-de-la-ficha.md) | Cargar el video e imágenes del ejercicio | RF_017, RF_018 | Material visual que facilita reproducir el ejercicio correctamente |
| [HU-09](HU-09-metadatos-academicos-de-la-ficha.md) | Registrar los metadatos académicos de la ficha | RF_019, RF_020, RF_021 | Trazabilidad académica de quién elaboró y aprobó cada ficha |

## Módulo 3 — Ciclo de vida de publicación de la ficha

RF_025 (validación antes de publicar) no es una historia propia: queda como criterio
de aceptación dentro de HU-11, ya que es una regla del sistema aplicada al publicar.

| ID | Título | Requerimientos cubiertos | Valor de negocio |
|---|---|---|---|
| [HU-10](HU-10-guardar-ficha-como-borrador.md) | Guardar la ficha como borrador | RF_024 | Evita perder trabajo y evita exponer contenido incompleto a los estudiantes |
| [HU-11](HU-11-publicar-ficha-validada.md) | Publicar una ficha validada | RF_025, RF_026 | Garantiza que solo se publique contenido clínicamente completo |
| [HU-12](HU-12-editar-ficha-publicada.md) | Editar una ficha publicada | RF_027 | Permite corregir/actualizar contenido sin despublicar ni duplicar la ficha |
| [HU-13](HU-13-eliminar-ficha-publicada.md) | Eliminar una ficha publicada | RF_048 | Retira del atlas contenido que no debería existir |
| [HU-14](HU-14-archivar-ficha-publicada.md) | Archivar una ficha publicada | RF_049 | Retiro reversible de contenido, sin perder la información |
| [HU-15](HU-15-gestion-compartida-de-fichas.md) | Gestionar fichas creadas por otros administradores | RF_050 | Permite el trabajo colaborativo entre docentes sobre el mismo contenido |

## Módulo 4 — Autenticación y cuenta de usuario

| ID | Título | Requerimientos cubiertos | Valor de negocio |
|---|---|---|---|
| [HU-16](HU-16-iniciar-sesion.md) | Iniciar sesión en el atlas | RF_028 | Puerta de entrada obligatoria a todo el sistema |
| [HU-17](HU-17-autorregistro-con-rol.md) | Crear una cuenta y seleccionar mi rol | RF_046, RF_054 | Crecimiento autónomo de la base de usuarios, con datos de rol para métricas |
| [HU-18](HU-18-recuperar-contrasena.md) | Recuperar contraseña olvidada | RF_047 | Evita que un usuario quede bloqueado fuera de su cuenta |
| [HU-19](HU-19-cierre-de-sesion-con-periodo-de-gracia.md) | Cierre automático de sesión con periodo de gracia | RF_029, RF_030 | Protege cuentas en dispositivos compartidos sin penalizar cierres accidentales |
| [HU-20](HU-20-aviso-de-cambios-sin-guardar.md) | Aviso de cambios sin guardar al cerrar la página | RF_031 | Evita pérdida de trabajo, sobre todo en el formulario largo de la ficha |

## Módulo 5 — Comentarios

RF_038 (cola de comentarios ocultados), RF_043 (relación ejercicio-comentario), RF_044
(relación usuario-comentario) y RF_053 (relación comentario-respuesta) no son historias
propias: quedan como criterios de aceptación dentro de la historia funcional a la que
pertenecen (ver "Supuestos" de cada HU).

| ID | Título | Requerimientos cubiertos | Valor de negocio |
|---|---|---|---|
| [HU-21](HU-21-escribir-y-consultar-comentarios.md) | Escribir y consultar comentarios en un ejercicio | RF_033, RF_036 | Habilita la interacción y el aprendizaje colaborativo alrededor de cada ejercicio |
| [HU-22](HU-22-editar-mi-comentario.md) | Editar mi propio comentario | RF_034 | Permite corregir o precisar lo dicho sin borrar y reescribir |
| [HU-23](HU-23-eliminar-mi-comentario.md) | Eliminar mi propio comentario | RF_035 | Da control al usuario sobre su propio contenido |
| [HU-24](HU-24-ocultamiento-automatico-de-contenido-ofensivo.md) | Ocultar automáticamente comentarios con contenido ofensivo | RF_037, RF_045 | Protege a los usuarios de contenido ofensivo mientras se revisa |
| [HU-25](HU-25-reportar-comentario.md) | Reportar un comentario ofensivo | RF_039 | Complementa el filtro automático con moderación de la comunidad |
| [HU-26](HU-26-revisar-comentarios-ocultados-y-reportados.md) | Revisar comentarios ocultados y reportados | RF_038, RF_040 | Punto central de moderación para el administrador |
| [HU-27](HU-27-aceptar-comentario-en-revision.md) | Aceptar un comentario en revisión | RF_041 | Corrige falsos positivos del filtro y cierra reportes infundados |
| [HU-28](HU-28-eliminar-cualquier-comentario.md) | Eliminar cualquier comentario como administrador | RF_042 | Control total del administrador para mantener limpio el espacio de comentarios |
| [HU-29](HU-29-responder-a-un-comentario.md) | Responder a un comentario | RF_052, RF_053 | Permite conversaciones enfocadas dentro de los comentarios |

## Módulo 6 — Métricas de uso

**Corrección de alcance:** RF_032 ("Registro de visitas") había quedado fuera de todos
los módulos anteriores por un error de agrupación inicial. Al no ser una historia
propia (es la captura de datos, no una funcionalidad visible), se incorporó aquí como
criterio de aceptación de HU-31, que es la métrica que consume esos datos.

| ID | Título | Requerimientos cubiertos | Valor de negocio |
|---|---|---|---|
| [HU-30](HU-30-acceder-al-panel-de-metricas.md) | Acceder al panel de métricas de uso | RF_055 | Punto único para consultar el uso real del atlas |
| [HU-31](HU-31-metrica-de-visitas-por-periodo.md) | Ver la métrica de visitas totales por periodo | RF_032, RF_056 | Indicador clave de adopción del atlas |
| [HU-32](HU-32-metrica-de-ejercicios-mas-consultados.md) | Ver la métrica de ejercicios más consultados | RF_057 | Ayuda a priorizar qué contenido mantener o mejorar |
| [HU-33](HU-33-metrica-de-usuarios-activos-por-rol.md) | Ver la métrica de usuarios activos por rol | RF_058 | Entender la composición real de la audiencia |
| [HU-34](HU-34-metrica-de-tiempo-promedio-de-uso.md) | Ver la métrica de tiempo promedio de uso por sesión | RF_059 | Mide la profundidad de uso, no solo la cantidad de visitas |

## Módulos pendientes

- Módulo 7 — Requerimientos no funcionales (RNF_001–RNF_005)
