### HU-09: Registrar los metadatos académicos de la ficha

**Origen:** RF_019, RF_020, RF_021

**Como** administrador
**Quiero** registrar los estudiantes que crearon la ficha, el periodo académico (cohorte/generación) y la profesora que la revisó
**Para** mantener la trazabilidad académica de quién elaboró y aprobó cada ficha

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista el registro base de la ficha (HU-04); el desarrollo de esta sección puede avanzar en paralelo con datos de prueba.
- N — Negotiable: ✅ No impone si los campos son texto libre o selección de catálogos (por ejemplo, lista de docentes).
- V — Valuable: ✅ Da trazabilidad académica y de calidad sobre el origen y revisión de cada ficha.
- E — Estimable: ✅ Tres campos de metadatos con propósito administrativo claro.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que los tres campos quedan registrados y visibles para el administrador.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Metadatos académicos de la ficha

  Escenario: Registro de los metadatos académicos
    Dado que existe una ficha con su información general ya registrada
    Cuando el administrador ingresa los nombres de los estudiantes creadores, selecciona
      el periodo académico (cohorte/generación) y registra el nombre de la profesora revisora
    Entonces el sistema guarda los tres datos asociados a esa ficha

  Escenario: La ficha puede guardarse sin metadatos académicos completos
    Dado que el administrador está registrando una ficha
    Cuando deja vacíos los campos de estudiantes creadores, cohorte/generación o profesora revisora
    Entonces el sistema guarda la ficha sin bloquear el registro por la ausencia de esos campos
```

**Supuestos / preguntas abiertas**
- Los tres campos tienen prioridad "Medio" en el documento de requerimientos, por lo que se interpretan como opcionales al registrar la ficha (igual criterio aplicado en HU-07 para progresión y errores comunes).
- No se especifica si "cohorte/generación" se elige de un catálogo de periodos académicos ya existente o se ingresa como texto libre; se asumió selección de un catálogo (por ejemplo, "2026-1"), ya que el requerimiento dice "seleccionar".
- No se especifica si "profesora revisora" se limita a un catálogo de docentes registradas en el sistema o admite texto libre; se asumió texto libre por simplicidad, a confirmar si existe un módulo de gestión de docentes.

**Mockup**

![Mockup HU-09](./mockups/HU-09.png)

Prompt sugerido (Lovable / v0.dev):
> Formulario de ficha clínica, sección "Metadatos académicos": campo de texto (o lista de chips) para nombres de estudiantes creadores, selector desplegable de cohorte/generación (ej. "2026-1") y campo de texto para el nombre de la profesora revisora, todos marcados como opcionales.
