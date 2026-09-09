### HU-02: Ver la ficha completa de un ejercicio

**Origen:** RF_004

**Como** usuario del atlas
**Quiero** hacer clic sobre el nombre de un ejercicio y ver su ficha completa
**Para** conocer en detalle cómo aplicar ese ejercicio en la práctica clínica

**Validación INVEST**
- I — Independent: ✅ Se puede entregar y probar de forma independiente, siempre que exista al menos una ficha publicada.
- N — Negotiable: ✅ No impone cómo se estructura visualmente la ficha, solo que debe mostrarse completa.
- V — Valuable: ✅ Es el momento en que el usuario obtiene el valor real del atlas (el detalle clínico del ejercicio).
- E — Estimable: ✅ Alcance claro: mostrar el detalle de una ficha ya existente.
- S — Small: ✅ Una sola pantalla/vista.
- T — Testable: ✅ Se puede verificar que, al hacer clic, se muestra la ficha y sus secciones.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Visualización de la ficha completa de un ejercicio

  Escenario: Visualización de una ficha publicada desde el listado de ejercicios
    Dado que el usuario ve el listado de nombres de ejercicios de una región y categoría
    Y existe una ficha publicada para uno de esos ejercicios
    Cuando el usuario hace clic sobre el nombre del ejercicio
    Entonces el sistema muestra la ficha completa del ejercicio, incluyendo objetivo terapéutico,
      estructuras intervenidas, indicaciones, precauciones y contraindicaciones, posición del
      paciente, posición del fisioterapeuta, ejecución del movimiento, comandos verbales,
      dosificación, progresión, errores comunes, video e imágenes

  Escenario: Intento de visualizar una ficha que no está publicada
    Dado que un ejercicio existe únicamente como borrador y no ha sido publicado
    Cuando el usuario intenta acceder a la ficha de ese ejercicio
    Entonces el sistema no muestra la ficha y el ejercicio no aparece en el listado de ejercicios
```

**Supuestos / preguntas abiertas**
- Se asume que un ejercicio en estado "borrador" o "archivado" no es accesible por URL directa para un usuario no administrador (comportamiento consistente con RF_024 y RF_049), aunque el requerimiento no lo indica explícitamente para este caso puntual.

**Pantallas para mockup**

| # | Pantalla | Elementos mínimos |
|---|---|---|
| 04 | Ficha completa del ejercicio | Video o imagen destacada arriba; secciones: objetivo, estructuras intervenidas, indicaciones/contraindicaciones, posición del paciente y del fisioterapeuta, ejecución paso a paso, comandos verbales, dosificación, progresión, errores comunes; galería de imágenes al final |

Prompt sugerido (Lovable / v0.dev):
> Página de detalle de un ejercicio clínico de fisioterapia, con reproductor de video destacado arriba y secciones colapsables debajo: objetivo terapéutico, estructuras intervenidas, indicaciones y contraindicaciones, posición del paciente, posición del fisioterapeuta, pasos de ejecución del movimiento, comandos verbales, dosificación (series/repeticiones/descanso), progresión y errores comunes. Cierra con una galería de imágenes.
