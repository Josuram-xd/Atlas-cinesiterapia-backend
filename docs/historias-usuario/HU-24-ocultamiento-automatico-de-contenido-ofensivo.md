### HU-24: Ocultar automáticamente comentarios con contenido ofensivo

**Origen:** RF_037, RF_045

**Como** sistema del atlas
**Quiero** analizar cada comentario al enviarse y ocultarlo automáticamente si contiene palabras obscenas u ofensivas, mostrándoselo a su autor como "esperando revisión"
**Para** proteger a los demás usuarios de contenido ofensivo mientras un administrador lo revisa

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista la funcionalidad de publicar comentarios (HU-21).
- N — Negotiable: ✅ No impone el motor de detección (lista de palabras, servicio externo, etc.), solo el resultado observable.
- V — Valuable: ✅ Protege la calidad y seguridad del espacio de comentarios sin bloquear la conversación por completo.
- E — Estimable: ✅ Regla de comportamiento acotada y su estado visual asociado.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar qué ven los demás usuarios frente a lo que ve el autor del comentario oculto.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Ocultamiento automático de comentarios ofensivos

  Escenario: Un comentario con contenido ofensivo se oculta para los demás usuarios
    Dado que un usuario envía un comentario que contiene una palabra de la lista de
      términos prohibidos del sistema
    Cuando el sistema analiza el comentario al enviarse
    Entonces el sistema lo oculta automáticamente y no lo muestra a otros usuarios

  Escenario: El autor ve su propio comentario oculto en estado de revisión
    Dado que el comentario de un usuario fue ocultado automáticamente por el filtro
    Cuando ese usuario consulta la ficha del ejercicio donde lo escribió
    Entonces el sistema le muestra su comentario atenuado en gris, con un ícono de
      advertencia a la derecha y el mensaje "esperando revisión", en lugar de mostrarlo
      como un comentario publicado

  Escenario: Un comentario sin contenido ofensivo se publica directamente
    Dado que un usuario envía un comentario que no contiene ninguna palabra de la
      lista de términos prohibidos del sistema
    Cuando el sistema lo analiza al enviarse
    Entonces el comentario se publica de inmediato y es visible para todos los usuarios
```

**Supuestos / preguntas abiertas**
- No se especifica el mecanismo exacto de detección de contenido ofensivo; se asumió una lista de términos prohibidos configurable por el sistema, sin definir si es estática o gestionada por un servicio externo de moderación. Esto se define técnicamente al implementar, sin afectar el criterio de aceptación observable.
- El estado "esperando revisión" (RF_045) se interpretó como exclusivo de los comentarios ocultados automáticamente por este filtro, no de los comentarios reportados por otros usuarios (ver HU-25), ya que RF_045 no menciona el reporte manual.

**Mockup**

![Mockup HU-24](./mockups/HU-24.png)

Prompt sugerido (Lovable / v0.dev):
> Comentario propio en estado "esperando revisión": tarjeta de comentario visualmente atenuada en gris, con un ícono de advertencia (triángulo con exclamación) alineado a la derecha y el texto "Esperando revisión" en lugar del comentario mostrado normalmente. Visible solo para el autor del comentario.
