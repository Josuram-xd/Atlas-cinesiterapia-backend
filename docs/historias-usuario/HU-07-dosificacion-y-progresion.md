### HU-07: Registrar la dosificación, progresión y errores comunes del ejercicio

**Origen:** RF_014, RF_015, RF_016

**Como** administrador
**Quiero** registrar la dosificación del ejercicio y, opcionalmente, su progresión y los errores comunes de ejecución
**Para** indicar cómo prescribir el ejercicio de forma segura y cómo ajustarlo o corregirlo en la práctica

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista el registro base de la ficha (HU-04); el desarrollo de esta sección puede avanzar en paralelo con datos de prueba.
- N — Negotiable: ✅ No impone si la dosificación se ingresa como campos numéricos estructurados o como texto libre.
- V — Valuable: ✅ La dosificación es indispensable para prescribir el ejercicio de forma segura; progresión y errores comunes agregan valor clínico adicional.
- E — Estimable: ✅ Alcance acotado a tres campos con reglas de obligatoriedad distintas.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar qué campos son obligatorios y cuáles opcionales, y que los valores quedan guardados.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Dosificación, progresión y errores comunes del ejercicio

  Escenario: Registro de la dosificación del ejercicio
    Dado que existe una ficha con su información general ya registrada
    Cuando el administrador ingresa series, repeticiones, intensidad, tiempo, descanso y frecuencia
    Entonces el sistema guarda la dosificación asociada a esa ficha

  Escenario: Registro opcional de la progresión y los errores comunes
    Dado que una ficha ya tiene registrada su dosificación
    Cuando el administrador ingresa las modificaciones para aumentar la dificultad del ejercicio
      y las compensaciones biomecánicas habituales
    Entonces el sistema guarda la progresión y los errores comunes asociados a esa ficha

  Escenario: La ficha puede guardarse sin progresión ni errores comunes
    Dado que el administrador está completando la dosificación de una ficha
    Cuando deja vacíos los campos de progresión y de errores comunes
    Entonces el sistema guarda la ficha sin bloquear el registro por la ausencia de esos dos campos
```

**Supuestos / preguntas abiertas**
- No se especifica si series, repeticiones, tiempo, descanso e intensidad son campos numéricos con unidades fijas o texto libre; se asumió texto libre para dar flexibilidad clínica (por ejemplo, "3x12" o "hasta fatiga").
- Progresión (RF_015) y errores comunes (RF_016) tienen prioridad "Medio" en el documento de requerimientos, por lo que se interpretan como opcionales al registrar la ficha; su exigibilidad para publicar se define en la historia de validación antes de publicar (módulo "Ciclo de vida de publicación").

**Mockup**

![Mockup HU-07](./mockups/HU-07.png)

Prompt sugerido (Lovable / v0.dev):
> Formulario de ficha clínica, sección "Dosificación y progresión": campos para series, repeticiones, intensidad, tiempo, descanso y frecuencia; campo opcional de texto para progresión (cómo aumentar la dificultad) y campo opcional de texto para errores comunes/compensaciones biomecánicas, ambos marcados visualmente como opcionales.
