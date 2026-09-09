# Requerimientos — Atlas Interactivo de Cinesiterapia

## RF — Requerimientos Funcionales

| ID | Nombre | Descripción | Nivel de Prioridad | Actores | Multiplicidad |
|---|---|---|---|---|---|
| RF_001 | Selección de región anatómica | El sistema debe mostrar un cuerpo humano en vista anterior y posterior que permita seleccionar la región anatómica. | Alto | Usuario | — |
| RF_002 | Despliegue de categorías terapéuticas | El sistema debe mostrar el menú de las 9 categorías terapéuticas al seleccionar una región anatómica. | Alto | Sistema | — |
| RF_003 | Listado de ejercicios por categoría | El sistema debe mostrar el listado de nombres de ejercicios disponibles al seleccionar una categoría terapéutica. | Alto | Sistema | — |
| RF_004 | Visualización de ficha completa | El sistema debe mostrar la ficha completa del ejercicio al hacer clic sobre su nombre. | Alto | Usuario | — |
| RF_005 | Registro de información general | El sistema debe permitir registrar el nombre, la región anatómica y la categoría terapéutica del ejercicio. | Alto | Administrador | — |
| RF_006 | Registro del objetivo terapéutico | El sistema debe permitir registrar el objetivo terapéutico principal del ejercicio. | Alto | Administrador | — |
| RF_007 | Registro de estructuras intervenidas | El sistema debe permitir registrar músculos, articulaciones y ligamentos/tejidos blandos involucrados. | Alto | Administrador | — |
| RF_008 | Registro de indicaciones clínicas | El sistema debe permitir registrar las patologías o fases de rehabilitación donde está indicado el ejercicio. | Alto | Administrador | — |
| RF_009 | Registro de precauciones y contraindicaciones | El sistema debe permitir registrar las situaciones clínicas donde el ejercicio debe modificarse o evitarse. | Alto | Administrador | — |
| RF_010 | Registro de posición del paciente | El sistema debe permitir registrar la postura inicial del paciente. | Alto | Administrador | — |
| RF_011 | Registro de posición del fisioterapeuta | El sistema debe permitir registrar ubicación, tomas y fijaciones del fisioterapeuta. | Alto | Administrador | — |
| RF_012 | Registro de ejecución del movimiento | El sistema debe permitir registrar la descripción paso a paso del gesto motor. | Alto | Administrador | — |
| RF_013 | Registro de comandos verbales | El sistema debe permitir registrar las consignas verbales para el paciente. | Alto | Administrador | — |
| RF_014 | Registro de dosificación | El sistema debe permitir registrar series, repeticiones, intensidad, tiempo, descanso y frecuencia. | Alto | Administrador | — |
| RF_015 | Registro de progresión | El sistema debe permitir registrar las modificaciones para aumentar la dificultad del ejercicio. | Medio | Administrador | — |
| RF_016 | Registro de errores comunes | El sistema debe permitir registrar las compensaciones biomecánicas habituales. | Medio | Administrador | — |
| RF_017 | Carga y reproducción de mini video | El sistema debe permitir cargar y reproducir un mini video (MP4/WebM/GIF) del ejercicio. | Alto | Administrador / Usuario | — |
| RF_018 | Galería de imágenes | El sistema debe permitir cargar y visualizar imágenes de posición inicial, final y tomas del terapeuta. | Alto | Administrador / Usuario | — |
| RF_019 | Registro de estudiantes creadores | El sistema debe permitir registrar los nombres de los estudiantes que crearon la ficha. | Medio | Administrador | — |
| RF_020 | Selección de cohorte/generación | El sistema debe permitir seleccionar el periodo académico de la ficha. | Medio | Administrador | — |
| RF_021 | Registro de profesora revisora | El sistema debe permitir registrar el nombre de la docente que aprobó la ficha. | Medio | Administrador | — |
| RF_022 | Relación región-ejercicio | El sistema debe asociar cada ejercicio a una única región anatómica. | Alto | Sistema | Región Anatómica (1) — Ejercicio (0..*) |
| RF_023 | Relación categoría-ejercicio | El sistema debe asociar cada ejercicio a una única categoría terapéutica. | Alto | Sistema | Categoría Terapéutica (1) — Ejercicio (0..*) |
| RF_024 | Guardado como borrador | El sistema debe permitir guardar una ficha como borrador, sin mostrarla a los estudiantes. | Alto | Administrador | — |
| RF_025 | Validación antes de publicar | El sistema debe verificar que todos los campos obligatorios estén completos antes de permitir publicar la ficha. | Alto | Sistema | — |
| RF_026 | Publicación de ficha | El sistema debe permitir publicar una ficha validada, haciéndola visible para los estudiantes. | Alto | Administrador | — |
| RF_027 | Edición de ficha publicada | El sistema debe permitir que la profesora edite una ficha ya publicada. | Medio | Administrador | — |
| RF_028 | Inicio de sesión obligatorio | El sistema debe requerir inicio de sesión con correo y contraseña para ver o usar el atlas. | Alto | Usuario / Administrador | — |
| RF_029 | Cierre de sesión al cerrar página | El sistema debe cerrar la sesión del usuario cuando se cierra la página o pestaña del navegador. | Alto | Sistema | — |
| RF_030 | Periodo de gracia de sesión | El sistema debe esperar un minuto antes de invalidar la sesión tras un cierre accidental de la página. | Medio | Sistema | — |
| RF_031 | Aviso de cambios sin guardar | El sistema debe advertir al usuario si intenta cerrar la página con cambios sin guardar. | Medio | Sistema | — |
| RF_032 | Registro de visitas | El sistema debe registrar cada visita a la página, con fecha y usuario, para análisis de uso. | Medio | Sistema | — |
| RF_033 | Escritura de comentarios | El sistema debe permitir que un usuario con sesión iniciada escriba un comentario de texto en un ejercicio. | Alto | Usuario | — |
| RF_034 | Edición de comentario propio | El sistema debe permitir que un usuario edite su propio comentario. | Medio | Usuario | — |
| RF_035 | Eliminación de comentario propio | El sistema debe permitir que un usuario elimine su propio comentario. | Medio | Usuario | — |
| RF_036 | Visualización de comentarios | El sistema debe mostrar los comentarios publicados de un ejercicio a cualquier usuario con sesión iniciada. | Alto | Usuario | — |
| RF_037 | Filtro automático de contenido ofensivo | El sistema debe analizar cada comentario al enviarse y ocultarlo automáticamente si contiene palabras obscenas u ofensivas. | Alto | Sistema | — |
| RF_038 | Cola de comentarios ocultados | El sistema debe registrar los comentarios ocultados automáticamente para que el administrador los revise. | Alto | Sistema | — |
| RF_039 | Reporte de comentarios por usuarios | El sistema debe permitir que un usuario reporte el comentario de otro usuario como ofensivo. | Medio | Usuario | — |
| RF_040 | Página de revisión de comentarios | El sistema debe mostrar al administrador una página con los comentarios ocultados automáticamente y los reportados por usuarios. | Alto | Administrador | — |
| RF_041 | Aceptación de comentario en revisión | El sistema debe permitir que el administrador acepte un comentario en revisión, haciéndolo visible nuevamente. | Alto | Administrador | — |
| RF_042 | Eliminación de comentario por administrador | El sistema debe permitir que el administrador elimine cualquier comentario, esté o no marcado por el filtro automático. | Alto | Administrador | — |
| RF_043 | Relación ejercicio-comentario | El sistema debe asociar cada comentario a un único ejercicio, permitiendo que un ejercicio tenga varios comentarios. | Alto | Sistema | Ejercicio (1) — Comentario (0..*) |
| RF_044 | Relación usuario-comentario | El sistema debe asociar cada comentario a un único usuario autor, permitiendo que un usuario escriba varios comentarios. | Alto | Sistema | Usuario (1) — Comentario (0..*) |
| RF_045 | Estado visual de comentario en revisión | El sistema debe mostrar al autor su comentario en revisión atenuado en gris, con un ícono de advertencia a la derecha y el mensaje "esperando revisión", en lugar de mostrarlo como un comentario publicado. | Medio | Sistema | — |
| RF_046 | Autorregistro de estudiantes | El sistema debe permitir que un estudiante cree su propia cuenta con correo y contraseña. | Alto | Usuario | — |
| RF_047 | Recuperación de contraseña | El sistema debe permitir que un usuario restablezca su contraseña si la olvida. | Alto | Usuario | — |
| RF_048 | Eliminación de ficha publicada | El sistema debe permitir que la profesora elimine una ficha ya publicada. | Medio | Administrador | — |
| RF_049 | Archivado de ficha publicada | El sistema debe permitir que la profesora archive una ficha publicada, ocultándola del atlas sin eliminarla permanentemente. | Medio | Administrador | — |
| RF_050 | Gestión compartida entre administradores | El sistema debe permitir que cualquier administrador vea y edite las fichas creadas por cualquier otro administrador. | Medio | Administrador | — |
| RF_051 | Búsqueda de ejercicios por nombre | El sistema debe permitir buscar ejercicios por nombre. | Medio | Usuario | — |
| RF_052 | Respuesta a comentarios | El sistema debe permitir que un usuario responda directamente a un comentario existente, formando un hilo. | Medio | Usuario | — |
| RF_053 | Relación comentario-respuesta | El sistema debe asociar cada respuesta a un único comentario padre, permitiendo que un comentario tenga varias respuestas. | Medio | Sistema | Comentario (1) — Respuesta (0..*) |
| RF_054 | Registro de rol de usuario | El sistema debe permitir que el usuario seleccione su rol (Estudiante, Profesional, Practicante, Visitante/curioso) al registrarse. | Medio | Usuario | — |
| RF_055 | Página de métricas de calidad | El sistema debe mostrar al administrador una página de métricas de uso del atlas. | Alto | Administrador | — |
| RF_056 | Métrica de visitas totales por periodo | La página de métricas debe mostrar el total de visitas al atlas por periodo de tiempo. | Alto | Sistema | — |
| RF_057 | Métrica de ejercicios más consultados | La página de métricas debe mostrar los ejercicios más consultados por los usuarios. | Alto | Sistema | — |
| RF_058 | Métrica de usuarios activos por rol | La página de métricas debe mostrar el número de usuarios activos agrupados por rol. | Alto | Sistema | — |
| RF_059 | Métrica de tiempo promedio de uso | La página de métricas debe mostrar el tiempo promedio de uso del atlas por sesión. | Medio | Sistema | — |

## RNF — Requerimientos No Funcionales

| ID | Nombre | Descripción | Nivel de Prioridad | Actores | Tipo de RNF | Origen |
|---|---|---|---|---|---|---|
| RNF_001 | Autenticación por tokens | El sistema debe autenticar a los usuarios mediante un modelo basado en tokens. | Alto | Sistema | Seguridad | Técnico |
| RNF_002 | Protección de credenciales | El sistema debe proteger correo y contraseña mediante cifrado en almacenamiento y transmisión. | Alto | Sistema | Seguridad | Técnico |
| RNF_003 | Compatibilidad multidispositivo | El sistema debe visualizarse correctamente en computador, tablet y celular (responsive). | Alto | Sistema | Compatibilidad | Técnico |
| RNF_004 | Disponibilidad sin conexión | El sistema debe permitir consultar el atlas sin internet una vez cargado inicialmente. | Alto | Sistema | Disponibilidad | Técnico |
| RNF_005 | Soporte de usuarios concurrentes | El sistema debe soportar el uso simultáneo de múltiples estudiantes sin degradar el desempeño. | Alto | Sistema | Escalabilidad | Técnico |
