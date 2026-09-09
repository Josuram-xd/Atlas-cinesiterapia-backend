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

## Módulos pendientes

- Módulo 5 — Comentarios (RF_033–RF_045, RF_052, RF_053)
- Módulo 6 — Métricas de uso (RF_055–RF_059)
- Módulo 7 — Requerimientos no funcionales (RNF_001–RNF_005)
