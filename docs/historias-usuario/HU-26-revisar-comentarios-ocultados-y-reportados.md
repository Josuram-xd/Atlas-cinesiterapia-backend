### HU-26: Revisar comentarios ocultados y reportados

**Origen:** RF_038, RF_040

**Como** administrador
**Quiero** ver en una sola página los comentarios ocultados automáticamente por el filtro y los reportados por otros usuarios
**Para** moderar el contenido ofensivo o inapropiado del atlas de forma centralizada

**Validación INVEST**
- I — Independent: ⚠️ Depende de que existan comentarios ocultados por el filtro (HU-24) o reportados (HU-25) para tener contenido que revisar.
- N — Negotiable: ✅ No impone el diseño de la página, solo qué información debe reunir.
- V — Valuable: ✅ Es el punto central desde el que el administrador ejerce la moderación de comentarios.
- E — Estimable: ✅ Alcance acotado: un listado que combina dos orígenes de comentarios en revisión.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que ambos tipos de comentarios en revisión aparecen en la página.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Página de revisión de comentarios

  Escenario: La página muestra los comentarios ocultados automáticamente
    Dado que existen comentarios ocultados automáticamente por el filtro de contenido ofensivo
    Cuando el administrador abre la página de revisión de comentarios
    Entonces el sistema le muestra esos comentarios señalados como "ocultado automáticamente"

  Escenario: La página muestra los comentarios reportados por usuarios
    Dado que existen comentarios reportados por uno o más usuarios
    Cuando el administrador abre la página de revisión de comentarios
    Entonces el sistema le muestra esos comentarios señalados como "reportado por usuarios"

  Escenario: La página de revisión sin comentarios pendientes
    Dado que no existen comentarios ocultados ni reportados
    Cuando el administrador abre la página de revisión de comentarios
    Entonces el sistema muestra un mensaje indicando que no hay comentarios pendientes de revisión
```

**Supuestos / preguntas abiertas**
- RF_038 (registrar los comentarios ocultados para que el administrador los revise) no se documenta como historia propia: es la base de datos/cola que alimenta esta página de revisión, así que queda cubierta aquí como precondición.
- Se asumió que la página distingue visualmente el motivo por el que un comentario está en revisión (ocultado automáticamente vs. reportado), aunque el requerimiento no lo pide de forma explícita, para que el administrador priorice su revisión con criterio.

**Mockup**

![Mockup HU-26](./mockups/HU-26.png)

Prompt sugerido (Lovable / v0.dev):
> Panel de administración "Revisión de comentarios" con dos pestañas o secciones: "Ocultados automáticamente" y "Reportados por usuarios", cada fila mostrando el texto del comentario, el autor, el ejercicio al que pertenece y acciones de aceptar/eliminar.
