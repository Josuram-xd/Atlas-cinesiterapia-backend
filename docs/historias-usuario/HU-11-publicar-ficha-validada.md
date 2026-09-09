### HU-11: Publicar una ficha validada

**Origen:** RF_025, RF_026

**Como** administrador
**Quiero** publicar una ficha de ejercicio solo cuando todos sus campos obligatorios estén completos
**Para** garantizar que los estudiantes únicamente vean fichas clínicamente completas y confiables

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista una ficha en estado borrador (HU-10) con contenido cargado desde las historias del Módulo 2.
- N — Negotiable: ✅ No impone cómo se le comunica al administrador qué falta, solo que debe impedirse publicar contenido incompleto.
- V — Valuable: ✅ Protege la calidad y confiabilidad del contenido clínico que llega a los estudiantes.
- E — Estimable: ✅ Regla de validación acotada a una lista conocida de campos obligatorios.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar exactamente qué combinaciones de campos permiten o bloquean la publicación.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Publicación de una ficha validada

  Escenario: Publicación exitosa de una ficha completa
    Dado que una ficha en borrador tiene completos el nombre, región, categoría, objetivo
      terapéutico, estructuras intervenidas, indicaciones, precauciones y contraindicaciones,
      posición del paciente, posición del fisioterapeuta, ejecución del movimiento, comandos
      verbales, dosificación, video e imágenes
    Cuando el administrador intenta publicar la ficha
    Entonces el sistema publica la ficha y la hace visible para los estudiantes en el atlas

  Escenario: Intento de publicar una ficha con campos obligatorios incompletos
    Dado que una ficha en borrador tiene al menos un campo obligatorio sin completar
    Cuando el administrador intenta publicar la ficha
    Entonces el sistema impide la publicación y muestra los campos obligatorios pendientes

  Escenario: Una ficha con campos opcionales vacíos sí puede publicarse
    Dado que una ficha en borrador tiene todos los campos obligatorios completos
    Y tiene vacíos los campos opcionales (progresión, errores comunes, estudiantes
      creadores, cohorte/generación, profesora revisora)
    Cuando el administrador intenta publicar la ficha
    Entonces el sistema publica la ficha sin exigir esos campos opcionales
```

**Supuestos / preguntas abiertas**
- El documento de requerimientos no enumera explícitamente cuáles campos son "obligatorios" para publicar; se asumió que son los campos con prioridad "Alto" (nombre, región, categoría, objetivo, estructuras, indicaciones, precauciones, posición del paciente, posición del fisioterapeuta, ejecución, comandos verbales, dosificación, video, galería de imágenes), y que los de prioridad "Medio" (progresión, errores comunes, estudiantes creadores, cohorte/generación, profesora revisora) son opcionales. Esto debería confirmarse con la profesora antes de implementar la validación.

**Mockup**

![Mockup HU-11](./mockups/HU-11.png)

Prompt sugerido (Lovable / v0.dev):
> Botón principal "Publicar" en el formulario de ficha clínica. Al hacer clic con campos obligatorios incompletos, se muestra un panel de alerta listando los campos faltantes con enlaces para saltar a cada sección. Al completarse todos, el botón queda habilitado y tras publicar aparece una etiqueta de estado "Publicada · visible para estudiantes".
