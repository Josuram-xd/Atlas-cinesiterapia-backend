### HU-29: Responder a un comentario

**Origen:** RF_052, RF_053

**Como** usuario con sesión iniciada
**Quiero** responder directamente a un comentario existente
**Para** participar en la conversación sobre ese comentario específico, formando un hilo

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista un comentario publicado al cual responder (HU-21).
- N — Negotiable: ✅ No impone el diseño visual del hilo de respuestas.
- V — Valuable: ✅ Permite conversaciones enfocadas dentro de los comentarios de un ejercicio.
- E — Estimable: ✅ Alcance acotado a responder a un comentario existente.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que una respuesta queda asociada al comentario padre correcto.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Respuesta a comentarios

  Escenario: Publicación de una respuesta a un comentario existente
    Dado que existe un comentario publicado en la ficha de un ejercicio
    Cuando un usuario con sesión iniciada escribe una respuesta a ese comentario y la envía
    Entonces el sistema publica la respuesta asociada a ese comentario, formando un hilo

  Escenario: Un comentario puede tener varias respuestas
    Dado que un comentario ya tiene una respuesta publicada
    Cuando otro usuario responde también a ese mismo comentario
    Entonces el sistema muestra ambas respuestas dentro del mismo hilo, asociadas al
      mismo comentario padre

  Escenario: Las respuestas también pasan por el filtro automático de contenido ofensivo
    Dado que un usuario envía una respuesta que contiene contenido ofensivo
    Cuando el sistema la analiza al enviarse
    Entonces la respuesta se oculta automáticamente igual que un comentario nuevo
```

**Supuestos / preguntas abiertas**
- RF_053 (una respuesta pertenece a un único comentario padre, un comentario puede tener varias respuestas) no se documenta como historia propia: queda cubierta aquí, ya que toda respuesta se crea siempre asociada al comentario que se está respondiendo.
- Se asumió un hilo de un solo nivel (se puede responder a un comentario, pero no se especifica si se puede responder a una respuesta); el requerimiento habla de "formando un hilo" sin detallar niveles de anidamiento.

**Mockup**

![Mockup HU-29](./mockups/HU-29.png)

Prompt sugerido (Lovable / v0.dev):
> Comentario con un enlace "Responder" debajo del texto; al usarlo se abre un campo de texto corto, y las respuestas se muestran indentadas debajo del comentario padre, agrupadas visualmente como un hilo.
