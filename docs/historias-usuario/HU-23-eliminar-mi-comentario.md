### HU-23: Eliminar mi propio comentario

**Origen:** RF_035

**Como** usuario autor de un comentario
**Quiero** eliminar un comentario que yo mismo escribí
**Para** retirar algo que ya no quiero que esté publicado

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista un comentario propio ya publicado (HU-21).
- N — Negotiable: ✅ No impone el mecanismo de confirmación de borrado.
- V — Valuable: ✅ Da control al usuario sobre su propio contenido.
- E — Estimable: ✅ Acción acotada con efecto claro.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que solo el autor puede eliminar su propio comentario y que este deja de mostrarse.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Eliminación de comentario propio

  Escenario: Eliminación exitosa de un comentario propio
    Dado que el usuario "carla@correo.com" es autora de un comentario publicado
    Cuando elimina ese comentario
    Entonces el sistema lo elimina y deja de mostrarse en la ficha del ejercicio

  Escenario: Un usuario no puede eliminar el comentario de otro usuario
    Dado que el usuario "luis@correo.com" no es autor de un comentario publicado
    Cuando intenta eliminarlo
    Entonces el sistema no le permite eliminarlo
```

**Supuestos / preguntas abiertas**
- No se especifica qué ocurre con las respuestas (HU-29) de un comentario que su autor elimina; se asumió que las respuestas se conservan, mostrando el comentario padre como "eliminado por su autor", para no perder el hilo de conversación. A confirmar con el equipo.

**Mockup**

![Mockup HU-23](./mockups/HU-23.png)

Prompt sugerido (Lovable / v0.dev):
> Confirmación breve al elegir "Eliminar" en el menú de un comentario propio: modal pequeño "¿Eliminar este comentario? Esta acción no se puede deshacer", con botones "Cancelar" y "Eliminar".
