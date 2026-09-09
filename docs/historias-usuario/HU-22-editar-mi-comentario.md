### HU-22: Editar mi propio comentario

**Origen:** RF_034

**Como** usuario autor de un comentario
**Quiero** editar el texto de un comentario que yo mismo escribí
**Para** corregir o precisar lo que dije sin tener que borrarlo y volver a escribirlo

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista un comentario propio ya publicado (HU-21).
- N — Negotiable: ✅ No impone el mecanismo de edición (inline, modal, etc.).
- V — Valuable: ✅ Permite corregir errores o actualizar la información compartida.
- E — Estimable: ✅ Alcance acotado: modificar el texto de un comentario existente.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que solo el autor puede editar su propio comentario.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Edición de comentario propio

  Escenario: Edición exitosa de un comentario propio
    Dado que el usuario "carla@correo.com" es autora de un comentario publicado
    Cuando edita el texto de ese comentario y guarda los cambios
    Entonces el sistema actualiza el comentario con el nuevo texto

  Escenario: Un usuario no puede editar el comentario de otro usuario
    Dado que el usuario "luis@correo.com" no es autor de un comentario publicado
    Cuando intenta editar ese comentario
    Entonces el sistema no le permite editarlo

  Escenario: El comentario editado vuelve a pasar por el filtro automático
    Dado que el usuario "carla@correo.com" edita el texto de su comentario
    Cuando el nuevo texto contiene contenido ofensivo
    Entonces el sistema oculta el comentario editado igual que si fuera recién enviado
```

**Supuestos / preguntas abiertas**
- Se asume que editar un comentario lo vuelve a someter al filtro automático de contenido ofensivo (HU-24), interpretando que RF_037 ("analiza cada comentario al enviarse") también aplica al reenvío de una edición, aunque el requerimiento no lo menciona explícitamente para este caso.
- No se especifica si el comentario editado muestra alguna marca de "editado"; se deja como detalle de diseño abierto.

**Mockup**

![Mockup HU-22](./mockups/HU-22.png)

Prompt sugerido (Lovable / v0.dev):
> Comentario propio con un menú de opciones (ícono de tres puntos) que muestra "Editar" y "Eliminar" solo al autor. Al elegir "Editar", el texto del comentario se convierte en un campo editable con botones "Guardar" y "Cancelar".
