---
name: user-stories-invest-gherkin
description: Convierte requerimientos funcionales en historias de usuario validadas con INVEST, con criterios de aceptación en Gherkin **totalmente en español** (Característica/Escenario/Dado/Cuando/Entonces, no Feature/Scenario/Given/When/Then), y entrega el resultado como archivos .md (uno por historia, listos para descargar). Antes de generar cualquier historia, verifica si falta información bloqueante (actor, objetivo, reglas de negocio críticas, alcance) y, si falta, pregunta de forma puntual antes de continuar en vez de inventar contenido. Úsalo cuando el usuario pida "escribir historias de usuario", "convertir requerimientos en user stories", "redactar criterios de aceptación", "escribir escenarios Gherkin/BDD", "aplicar INVEST", o pegue requerimientos funcionales, actas o especificaciones y pida convertirlos en backlog. También úsalo si pide auditar o mejorar historias ya existentes contra INVEST o su Gherkin, aunque no use exactamente estas palabras.
---

# Historias de usuario con INVEST y Gherkin

Skill para transformar requerimientos funcionales (texto libre, actas, especificaciones,
listas de features) en historias de usuario correctamente formadas, validadas contra los
criterios INVEST, con criterios de aceptación en formato Gherkin.

## Cuándo usar este skill

- El usuario pega uno o más requerimientos funcionales y pide historias de usuario.
- El usuario pide criterios de aceptación / escenarios BDD para una funcionalidad.
- El usuario pide auditar, revisar o "arreglar" historias de usuario existentes.
- El usuario pide convertir un documento (PDF/Word/acta) de requerimientos en un backlog.
  En ese caso, primero extrae/lee el contenido del documento (usa el skill de lectura
  correspondiente: `pdf-reading`, `docx`, `file-reading`, etc.) y luego aplica este flujo.

No uses este skill para escribir código, diseñar arquitectura técnica, ni para historias
que no tengan relación con requerimientos de producto/negocio.

## Flujo de trabajo

Sigue estos pasos en orden. No te saltes la validación INVEST ni el Gherkin: son el
núcleo del skill.

### Paso 1 — Entender el/los requerimiento(s)

Lee el requerimiento funcional entregado por el usuario. Para cada uno identifica:

- **Actor(es) / rol(es)** involucrados (usuario final, administrador, sistema externo, etc.)
- **Acción o funcionalidad** esperada
- **Objetivo / valor de negocio** detrás de la funcionalidad (el "para qué")
- **Reglas de negocio, restricciones y condiciones** explícitas o implícitas
  (validaciones, permisos, límites, mensajes de error, casos límite)

Si el usuario entrega varios requerimientos a la vez, procesa cada uno por separado y
numera las historias resultantes de forma consecutiva (HU-01, HU-02, ...).

### Paso 1.5 — Verificar que hay suficiente información antes de escribir nada (gate obligatorio)

No pases al Paso 2 sin haber revisado este checklist. Para cada requerimiento, evalúa si
falta algo de lo siguiente:

1. **Actor no identificable**: no queda claro quién ejecuta la acción (¿usuario final?
   ¿administrador? ¿un sistema externo/batch?).
2. **Objetivo/valor ausente**: el requerimiento dice el "qué" pero no el "para qué", y
   no es deducible razonablemente del contexto.
3. **Regla de negocio crítica no definida**: existen decisiones que cambian el
   comportamiento observable y no están explícitas — p. ej. qué pasa en el caso de
   error, límites numéricos (intentos, tiempos, tamaños), permisos/roles que pueden o
   no ejecutar la acción, valores por defecto, mensajes exactos al usuario.
4. **Alcance ambiguo**: el requerimiento podría interpretarse como una sola historia o
   como varias funcionalidades distintas, y la interpretación cambia sustancialmente el
   resultado.
5. **Términos del dominio no definidos**: se usan palabras clave propias del negocio
   (estados, tipos, categorías) sin definición y el significado no es obvio ni estándar.
6. **Contradicción**: dos partes del requerimiento (o el requerimiento vs. algo dicho
   antes en la conversación) se contradicen.

Clasifica el nivel de impacto de lo que falta:

- **Bloqueante** (no se puede escribir una historia mínimamente correcta y testeable sin
  esa información — normalmente afecta el criterio Testable o Valuable de INVEST, o
  cambia el número de historias a crear): **debes preguntar antes de continuar.**
- **Menor** (afecta detalles de implementación de los escenarios pero no cambia el
  sentido de la historia, p. ej. el texto exacto de un mensaje de error): puedes asumir
  algo razonable y continuar, declarándolo en "Supuestos / preguntas abiertas".

Cómo preguntar cuando algo es bloqueante:
- Agrupa todas las preguntas bloqueantes de ese requerimiento en una sola ronda (no
  preguntes una por una en turnos separados).
- Haz preguntas concretas y cerradas cuando sea posible (opciones, sí/no, valores), no
  preguntas abiertas tipo "¿puedes darme más contexto?".
- Si el requerimiento tiene varias partes y solo una parte tiene bloqueos, dilo
  explícitamente: procede a generar las historias que sí tienen información suficiente,
  y pregunta solo sobre la parte bloqueada.
- Nunca generes una historia "de relleno" con datos inventados para una regla de
  negocio crítica solo para no preguntar — eso rompe Valuable y Testable de INVEST y
  produce criterios de aceptación falsos.

Ejemplo de formato para preguntar (usa esta estructura, no una lista larga de dudas
genéricas):

```markdown
Antes de generar la(s) historia(s), necesito confirmar lo siguiente:

1. [Pregunta puntual sobre el punto bloqueante 1] — ¿[opción A] o [opción B]?
2. [Pregunta puntual sobre el punto bloqueante 2]?

El resto del requerimiento es suficientemente claro; en cuanto confirmes esto,
genero la(s) historia(s) completas.
```

Si el usuario responde solo parte de las preguntas, procede con lo que sí quedó claro,
asume lo mínimo razonable para lo que sigue sin responder y decláralo como supuesto.

### Paso 2 — Descomponer si es necesario

Si un requerimiento mezcla más de una funcionalidad independiente, o es demasiado amplio
para completarse en una iteración corta, sepáralo en varias historias de usuario más
pequeñas (esto protege el criterio **Small** de INVEST). Prefiere dividir por:
- Flujo principal vs. flujos alternativos complejos
- Reglas de negocio independientes entre sí
- Distintos actores/roles

No dividas artificialmente algo que forma una sola unidad de valor coherente.

### Paso 3 — Redactar cada historia de usuario

Formato estándar obligatorio:

```
Como [rol/actor]
Quiero [acción/funcionalidad]
Para [beneficio/objetivo de negocio]
```

Reglas de redacción:
- Lenguaje claro, orientado a negocio, sin jerga técnica innecesaria (la jerga técnica
  de dominio sí puede aparecer en los escenarios Gherkin si es parte del vocabulario
  del negocio).
- Describe el "qué" y el "para qué", nunca el "cómo" técnico (evita mencionar tablas,
  endpoints, frameworks, nombres de clases, etc.).
- Un único actor y una única funcionalidad por historia.

### Paso 4 — Validar contra INVEST

Antes de dar una historia por terminada, verifica explícitamente cada criterio.
Si algo falla, corrige la historia (reformula, divide, agrega contexto) y vuelve a
validar. No presentes una historia final sin haber pasado por este check:

| Criterio | Pregunta de validación |
|---|---|
| **I — Independent** | ¿Se puede desarrollar y entregar sin depender obligatoriamente de otra historia? |
| **N — Negotiable** | ¿Describe el qué/para qué sin imponer una solución técnica específica, dejando espacio a conversación con el equipo? |
| **V — Valuable** | ¿Aporta valor claro y verificable al usuario final o al negocio? |
| **E — Estimable** | ¿Tiene suficiente claridad y contexto para que el equipo pueda estimar el esfuerzo? |
| **S — Small** | ¿Es lo bastante pequeña para completarse en una iteración corta (p. ej. un sprint)? |
| **T — Testable** | ¿Tiene (o puede tener) criterios de aceptación objetivamente verificables (sí/no)? |

Reporta el resultado de esta validación de forma breve (✅ cumple / ⚠️ requiere ajuste,
con una frase de justificación) — ver formato de salida abajo.

### Paso 5 — Escribir los criterios de aceptación en Gherkin (en español)

Convierte el comportamiento esperado en escenarios Gherkin **usando las palabras clave
en español**, no en inglés. Gherkin soporta localización nativa: agrega siempre la
directiva `# language: es` como primera línea del bloque, y usa esta equivalencia:

| Inglés | Español (usar siempre) |
|---|---|
| `Feature:` | `Característica:` |
| `Scenario:` | `Escenario:` |
| `Scenario Outline:` | `Esquema del escenario:` |
| `Given` | `Dado` / `Dado que` |
| `When` | `Cuando` |
| `Then` | `Entonces` |
| `And` | `Y` |
| `But` | `Pero` |
| `Examples:` | `Ejemplos:` |
| `Background:` | `Antecedentes:` |

Estructura estándar a usar:

```gherkin
# language: es
Característica: [Nombre de la funcionalidad]

  Escenario: [Nombre descriptivo y específico del escenario]
    Dado [contexto o precondición]
    Y [precondición adicional, si aplica]
    Cuando [acción realizada por el actor]
    Entonces [resultado esperado, verificable]
    Y [resultado adicional, si aplica]
```

Reglas:
- Incluye **siempre** al menos un escenario de flujo principal (happy path).
- Incluye escenarios alternativos y de error/excepción cuando el requerimiento lo
  amerite: validaciones de datos, permisos insuficientes, datos vacíos/nulos, límites,
  duplicados, timeouts, etc. No los inventes si el requerimiento no da pie a ellos;
  en ese caso decláralo como supuesto abierto (ver Paso 6).
- Usa `Esquema del escenario:` + `Ejemplos:` cuando el mismo flujo se repita con
  distintos datos de entrada/salida, para evitar escenarios duplicados.
- Cada paso `Entonces` debe ser objetivamente verificable (evita "Entonces el sistema
  funciona bien"; usa "Entonces el sistema muestra el mensaje 'Registro exitoso'").
- Todo el bloque va en español: palabras clave, nombres de escenario, textos de pasos,
  nombres de columnas en `Ejemplos:` y mensajes citados. No mezcles inglés y español
  dentro de un mismo bloque Gherkin.
- No mezcles varias `Característica:` en un mismo bloque; una historia = una
  Característica.
- Excepción: si el usuario pide explícitamente Gherkin en inglés (u otro idioma), usa
  las palabras clave de ese idioma en su lugar y ajusta `# language:` en consecuencia.


### Paso 6 — Registrar supuestos menores

Todo lo que en el Paso 1.5 clasificaste como "menor" (no bloqueante) y decidiste asumir
para poder avanzar, debe quedar explícito en la sección "Supuestos / preguntas abiertas"
de cada historia — nunca lo dejes implícito solo en el Gherkin. Esto le permite al
usuario corregir rápido sin tener que releer todo el detalle.

Recuerda: si algo era bloqueante, ya debiste haberlo preguntado en el Paso 1.5 antes de
llegar aquí. Este paso es solo para el registro de lo no bloqueante.

### Paso 7 — Entregar las historias como archivos .md

Una vez que las historias están redactadas, validadas contra INVEST y con su Gherkin
completo, el entregable final **debe ser en archivo(s) Markdown (.md)**, no solo texto
en el chat. Sigue estas reglas:

- Crea **un archivo `.md` por historia de usuario**, no un único archivo con todas
  amontonadas — así cada historia se puede cargar o compartir de forma independiente
  (p. ej. adjuntarla a un ticket de Jira/Azure DevOps).
- Nombre de archivo: `HU-[N]-[slug-corto-del-titulo].md`, en minúsculas y con guiones,
  sin tildes ni caracteres especiales. Ejemplo: `HU-01-recuperacion-de-contrasena.md`.
- El contenido de cada archivo usa exactamente la misma estructura definida en
  "Formato de salida" (Como/Quiero/Para, Validación INVEST, Gherkin, Supuestos).
- Si se generaron varias historias a partir de uno o más requerimientos, crea además
  un archivo `00-resumen.md` con la tabla resumen (ID, título, valor de negocio) que
  enlace o liste los nombres de los demás archivos.
- Después de crear los archivos, preséntalos todos al usuario para que pueda
  descargarlos — no dejes archivos creados sin presentar.
- Igual que en el chat, primero muestra el contenido de las historias en la
  conversación (formato de salida habitual) y luego entrega los archivos .md
  correspondientes; no reemplaces la respuesta conversacional por únicamente los
  archivos.
- Si el entorno no soporta creación de archivos, dilo explícitamente y entrega el
  contenido completo en bloques Markdown dentro del chat como alternativa, indicando
  que el usuario puede copiarlo a un archivo `.md` manualmente.

## Formato de salida

Para cada historia de usuario, entrega exactamente esta estructura:

```markdown
### HU-[N]: [Título corto]

**Como** [rol]
**Quiero** [funcionalidad]
**Para** [beneficio]

**Validación INVEST**
- I — Independent: ✅/⚠️ [justificación breve]
- N — Negotiable: ✅/⚠️ [justificación breve]
- V — Valuable: ✅/⚠️ [justificación breve]
- E — Estimable: ✅/⚠️ [justificación breve]
- S — Small: ✅/⚠️ [justificación breve]
- T — Testable: ✅/⚠️ [justificación breve]

**Criterios de aceptación (Gherkin)**
​```gherkin
# language: es
Característica: ...
  Escenario: ...
    Dado ...
    Cuando ...
    Entonces ...
​```

**Supuestos / preguntas abiertas**
- [solo si aplica]
```

Si se generan varias historias, preséntalas en orden numerado (HU-01, HU-02, ...) y,
si tiene sentido, cierra con una tabla resumen (ID, título, valor de negocio) para
facilitar la carga a un backlog (Jira, Azure DevOps, etc.).

## Ejemplo de referencia rápido

**Requerimiento funcional de entrada:**
"El sistema debe permitir a un cliente registrado recuperar su contraseña ingresando
su correo electrónico. Si el correo existe, se envía un enlace de recuperación válido
por 30 minutos. Si el correo no existe, se muestra un mensaje genérico sin revelar si
el correo está registrado."

**Salida esperada (resumen):**

```markdown
### HU-01: Recuperación de contraseña vía correo electrónico

**Como** cliente registrado
**Quiero** solicitar la recuperación de mi contraseña ingresando mi correo electrónico
**Para** poder recuperar el acceso a mi cuenta cuando la olvido

**Validación INVEST**
- I — Independent: ✅ No depende de otra historia para entregarse.
- N — Negotiable: ✅ No impone solución técnica (no menciona proveedor de correo, tokens, etc.).
- V — Valuable: ✅ Resuelve un bloqueo real de acceso del usuario.
- E — Estimable: ✅ Reglas de negocio suficientemente claras para estimar.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Tiene condiciones verificables (correo existe/no existe, expiración).

**Criterios de aceptación (Gherkin)**
​```gherkin
# language: es
Característica: Recuperación de contraseña

  Escenario: Solicitud de recuperación con correo registrado
    Dado que el usuario "cliente@ejemplo.com" está registrado en el sistema
    Cuando solicita la recuperación de contraseña ingresando su correo
    Entonces el sistema envía un enlace de recuperación al correo ingresado
    Y el enlace es válido por 30 minutos

  Escenario: Solicitud de recuperación con correo no registrado
    Dado que el correo "noexiste@ejemplo.com" no está registrado en el sistema
    Cuando se solicita la recuperación de contraseña con ese correo
    Entonces el sistema muestra un mensaje genérico de confirmación
    Y no se revela si el correo existe o no en el sistema

  Escenario: Expiración del enlace de recuperación
    Dado que un usuario registrado solicitó un enlace de recuperación
    Y han transcurrido más de 30 minutos desde el envío
    Cuando el usuario intenta usar el enlace
    Entonces el sistema rechaza el enlace por expiración
    Y solicita al usuario generar una nueva solicitud

**Supuestos / preguntas abiertas**
- No se especifica el mensaje exacto de error/confirmación; se usó uno genérico.
- No se especifica si hay límite de reintentos de solicitud por unidad de tiempo.
```

Usa este ejemplo como referencia de nivel de detalle y formato, no lo copies literal
para requerimientos distintos.

## Ejemplo de referencia — caso con información bloqueante

**Requerimiento funcional de entrada:**
"El sistema debe permitir cancelar pedidos."

**Respuesta esperada (no generar historias todavía):**

```markdown
Antes de generar la(s) historia(s), necesito confirmar lo siguiente:

1. ¿Quién puede cancelar un pedido: el cliente que lo hizo, un administrador, o ambos
   con reglas distintas?
2. ¿Un pedido se puede cancelar en cualquier estado, o solo antes de cierto punto
   (por ejemplo, antes de ser despachado)?
3. ¿Qué debe pasar con el pago cuando se cancela (reembolso automático, manual, no
   aplica)?

El resto del requerimiento es suficientemente claro; en cuanto confirmes esto, genero
la(s) historia(s) completas con sus criterios de aceptación en Gherkin.
```

Este es el comportamiento correcto: el requerimiento original no permite construir
criterios de aceptación testeables (Paso 1.5, puntos 1 y 3 son bloqueantes), así que el
skill pregunta antes de inventar reglas de negocio que no fueron dadas.