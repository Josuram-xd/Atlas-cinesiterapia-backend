### HU-06: Registrar la ejecución del ejercicio

**Origen:** RF_010, RF_011, RF_012, RF_013

**Como** administrador
**Quiero** registrar la posición del paciente, la posición del fisioterapeuta, la ejecución paso a paso del movimiento y los comandos verbales
**Para** que quien consulte la ficha pueda reproducir correctamente el ejercicio en la práctica clínica

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista el registro base de la ficha (HU-04); el desarrollo de esta sección puede avanzar en paralelo con datos de prueba.
- N — Negotiable: ✅ No impone el tipo de editor de texto ni la estructura visual de los pasos.
- V — Valuable: ✅ Es la guía operativa que permite ejecutar el ejercicio correctamente; es el contenido más consultado en la práctica.
- E — Estimable: ✅ Cuatro campos de texto con un propósito bien definido cada uno.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que cada campo quedó registrado y se muestra correctamente al consultar la ficha.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Ejecución del ejercicio

  Escenario: Registro completo de la ejecución del ejercicio
    Dado que existe una ficha con su información general ya registrada
    Cuando el administrador ingresa la posición inicial del paciente, la ubicación,
      tomas y fijaciones del fisioterapeuta, la descripción paso a paso del gesto motor
      y los comandos verbales para el paciente
    Entonces el sistema guarda los cuatro campos asociados a esa ficha

  Escenario: Edición de la ejecución de una ficha existente
    Dado que una ficha ya tiene registrada su ejecución
    Cuando el administrador modifica la posición del paciente, la posición del
      fisioterapeuta, la ejecución del movimiento o los comandos verbales
    Entonces el sistema actualiza la ficha con los nuevos valores
```

**Supuestos / preguntas abiertas**
- No se especifica si la "ejecución paso a paso" debe ingresarse como una lista numerada estructurada o como texto libre; se asumió texto libre que el administrador puede estructurar en pasos manualmente.
- No se especifica un límite de longitud para ninguno de los cuatro campos.

**Mockup**

![Mockup HU-06](./mockups/HU-06.png)

Prompt sugerido (Lovable / v0.dev):
> Formulario de ficha clínica, sección "Ejecución del ejercicio": campo de texto para posición del paciente, campo de texto para posición del fisioterapeuta (ubicación, tomas, fijaciones), editor de texto enriquecido o lista para la ejecución paso a paso del movimiento, y campo de texto para los comandos verbales al paciente.

**Mockup**

![Mockup HU-06](./mockups/HU-06.png)

Prompt sugerido (Lovable / v0.dev):
> Formulario de ficha clínica, sección "Ejecución del ejercicio": campo de texto para posición del paciente, campo de texto para posición del fisioterapeuta (ubicación, tomas, fijaciones), editor de texto enriquecido o lista para la ejecución paso a paso del movimiento, y campo de texto para los comandos verbales al paciente.
