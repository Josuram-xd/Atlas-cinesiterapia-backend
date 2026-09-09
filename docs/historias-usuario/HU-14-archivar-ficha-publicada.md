### HU-14: Archivar una ficha publicada

**Origen:** RF_049

**Como** administrador (profesora)
**Quiero** archivar una ficha publicada, ocultándola del atlas sin eliminarla permanentemente
**Para** retirar temporalmente del atlas un ejercicio que ya no debe mostrarse, conservando su contenido por si se vuelve a necesitar

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista una ficha publicada (HU-11).
- N — Negotiable: ✅ No impone el mecanismo de archivado, solo el resultado: la ficha se oculta pero no se destruye.
- V — Valuable: ✅ Da una alternativa reversible a la eliminación permanente para retirar contenido del atlas.
- E — Estimable: ✅ Acción acotada: cambia el estado de la ficha sin afectar su contenido.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que una ficha archivada deja de ser visible pero su contenido sigue existiendo en el sistema.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Archivado de una ficha publicada

  Escenario: Archivado exitoso de una ficha publicada
    Dado que existe una ficha en estado publicado
    Cuando el administrador archiva la ficha
    Entonces el sistema cambia la ficha a estado archivado
    Y la ficha deja de aparecer en los listados y búsquedas del atlas para los estudiantes

  Escenario: El contenido de una ficha archivada se conserva
    Dado que existe una ficha en estado archivado
    Cuando el administrador consulta la lista de fichas archivadas
    Entonces el sistema muestra la ficha con todo su contenido intacto
```

**Supuestos / preguntas abiertas**
- El requerimiento no indica si una ficha archivada puede volver a publicarse (des-archivarse); se documenta aquí como pregunta abierta para el equipo, ya que no se generó una historia de "reactivar ficha" al no estar solicitada explícitamente.
- Se asume que existe una vista o filtro donde el administrador puede consultar las fichas archivadas (distinta del atlas público), aunque el requerimiento no la describe en detalle.

**Mockup**

![Mockup HU-14](./mockups/HU-14.png)

Prompt sugerido (Lovable / v0.dev):
> Menú de acciones sobre una ficha publicada (en la vista de administración) con la opción "Archivar", y una vista aparte "Fichas archivadas" que lista las fichas ocultas del atlas con una etiqueta "Archivada" y su contenido accesible en modo solo lectura.
