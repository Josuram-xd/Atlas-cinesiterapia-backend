### HU-13: Eliminar una ficha publicada

**Origen:** RF_048

**Como** administrador (profesora)
**Quiero** eliminar definitivamente una ficha que ya está publicada
**Para** retirar del atlas contenido que ya no debe existir (por ejemplo, duplicado o creado por error)

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista una ficha publicada (HU-11).
- N — Negotiable: ✅ No impone el mecanismo de confirmación ni el diseño del diálogo de borrado.
- V — Valuable: ✅ Permite mantener el atlas libre de contenido que no debería existir.
- E — Estimable: ✅ Acción acotada con efecto claro (la ficha deja de existir).
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que la ficha eliminada deja de estar disponible en el sistema.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Eliminación de una ficha publicada

  Escenario: Eliminación exitosa de una ficha publicada
    Dado que existe una ficha en estado publicado
    Cuando el administrador elimina la ficha y confirma la acción
    Entonces el sistema elimina la ficha de forma permanente
    Y la ficha deja de aparecer en cualquier listado o búsqueda del atlas

  Escenario: Cancelación de la eliminación
    Dado que el administrador inició la eliminación de una ficha publicada
    Cuando cancela la confirmación de eliminación
    Entonces la ficha permanece publicada sin cambios
```

**Supuestos / preguntas abiertas**
- Se asume que la eliminación requiere una confirmación explícita del administrador antes de ejecutarse, por ser una acción irreversible; el requerimiento no lo indica pero es una práctica estándar para operaciones destructivas.
- Se asume que la eliminación es permanente (a diferencia del archivado en HU-14, que sí es reversible); el requerimiento distingue ambas acciones pero no detalla si la eliminación conserva algún respaldo interno.
- No se especifica qué ocurre con los comentarios asociados a una ficha eliminada; queda como punto a confirmar antes de implementar (afecta al módulo de Comentarios).

**Mockup**

![Mockup HU-13](./mockups/HU-13.png)

Prompt sugerido (Lovable / v0.dev):
> Modal de confirmación de eliminación sobre una ficha publicada: título "Eliminar ficha de forma permanente", texto de advertencia explicando que la acción no se puede deshacer, botón secundario "Cancelar" y botón destructivo "Eliminar definitivamente".
