### HU-27: Aceptar un comentario en revisión

**Origen:** RF_041

**Como** administrador
**Quiero** aceptar un comentario que está en revisión
**Para** que vuelva a ser visible con normalidad cuando, tras revisarlo, determino que no era ofensivo

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista la página de revisión de comentarios (HU-26) con contenido pendiente.
- N — Negotiable: ✅ No impone el mecanismo de aceptación, solo el resultado esperado.
- V — Valuable: ✅ Permite corregir falsos positivos del filtro automático y cerrar reportes infundados.
- E — Estimable: ✅ Acción acotada con efecto claro sobre el estado del comentario.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que, tras aceptarlo, el comentario vuelve a mostrarse como publicado.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Aceptación de un comentario en revisión

  Escenario: Aceptación de un comentario ocultado automáticamente
    Dado que un comentario está oculto por el filtro automático y en estado "esperando revisión"
    Cuando el administrador lo acepta desde la página de revisión de comentarios
    Entonces el sistema lo muestra nuevamente como un comentario publicado, visible
      para todos los usuarios, y deja de mostrarlo como "esperando revisión" a su autor

  Escenario: Aceptación de un comentario reportado
    Dado que un comentario reportado por usuarios está en la página de revisión de comentarios
    Cuando el administrador lo acepta
    Entonces el sistema lo retira de la página de revisión de comentarios, y el
      comentario continúa visible con normalidad como ya lo estaba
```

**Supuestos / preguntas abiertas**
- No se especifica si aceptar un comentario "entrena" o ajusta el filtro automático para casos similares en el futuro; se asumió que la aceptación solo afecta a ese comentario puntual.

**Mockup**

![Mockup HU-27](./mockups/HU-27.png)

Prompt sugerido (Lovable / v0.dev):
> Fila de comentario en el panel "Revisión de comentarios" con botón "Aceptar" (icono de check, color positivo) junto al botón "Eliminar". Al aceptar, la fila desaparece de la lista con una confirmación breve "Comentario aceptado".
