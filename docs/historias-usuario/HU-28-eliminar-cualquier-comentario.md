### HU-28: Eliminar cualquier comentario como administrador

**Origen:** RF_042

**Como** administrador
**Quiero** eliminar cualquier comentario del atlas, esté o no marcado por el filtro automático
**Para** retirar contenido inapropiado sin depender de que el autor lo elimine por su cuenta

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista al menos un comentario publicado (HU-21) sobre el cual actuar.
- N — Negotiable: ✅ No impone el mecanismo de confirmación de borrado.
- V — Valuable: ✅ Da a los administradores control total para mantener limpio el espacio de comentarios.
- E — Estimable: ✅ Acción acotada con efecto claro.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que el administrador puede eliminar cualquier comentario, publicado o en revisión.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Eliminación de cualquier comentario por un administrador

  Escenario: Eliminación de un comentario publicado
    Dado que existe un comentario publicado y visible en una ficha de ejercicio
    Cuando el administrador lo elimina
    Entonces el sistema lo elimina y deja de mostrarse en la ficha del ejercicio

  Escenario: Eliminación de un comentario en revisión
    Dado que existe un comentario en la página de revisión de comentarios (HU-26)
    Cuando el administrador lo elimina directamente desde esa página
    Entonces el sistema lo elimina y lo retira tanto de la ficha del ejercicio como de
      la página de revisión
```

**Supuestos / preguntas abiertas**
- No se especifica si se requiere confirmación explícita antes de eliminar; se asumió que sí, por tratarse de una acción irreversible, siguiendo el mismo criterio aplicado a la eliminación de fichas (HU-13).

**Mockup**

![Mockup HU-28](./mockups/HU-28.png)

Prompt sugerido (Lovable / v0.dev):
> Comentario visto por un administrador con un botón "Eliminar" siempre visible (a diferencia de un usuario normal, que solo lo ve en sus propios comentarios), con un modal de confirmación antes de eliminar.
