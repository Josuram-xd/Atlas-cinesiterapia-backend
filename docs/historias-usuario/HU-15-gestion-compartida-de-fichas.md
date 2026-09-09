### HU-15: Gestionar fichas creadas por otros administradores

**Origen:** RF_050

**Como** administrador
**Quiero** ver y editar las fichas de ejercicio creadas por cualquier otro administrador
**Para** poder colaborar en equipo en el mantenimiento del atlas sin depender de quién creó cada ficha originalmente

**Validación INVEST**
- I — Independent: ✅ Es una regla de acceso transversal, verificable con cualquier ficha existente sin importar el flujo que la creó.
- N — Negotiable: ✅ No impone cómo se muestra la autoría en la interfaz, solo que el acceso no debe estar restringido al creador original.
- V — Valuable: ✅ Permite el trabajo colaborativo entre docentes/administradores sobre el mismo contenido.
- E — Estimable: ✅ Regla de permisos acotada a un único rol (administrador) sobre todas las fichas.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que un administrador distinto al creador puede ver y editar la ficha.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Gestión compartida de fichas entre administradores

  Escenario: Un administrador visualiza una ficha creada por otro administrador
    Dado que la administradora "Ana" creó una ficha de ejercicio
    Cuando el administrador "Luis" consulta esa ficha en el panel de administración
    Entonces el sistema le permite visualizar el contenido completo de la ficha

  Escenario: Un administrador edita una ficha creada por otro administrador
    Dado que la administradora "Ana" creó una ficha de ejercicio
    Cuando el administrador "Luis" edita el contenido de esa ficha y guarda los cambios
    Entonces el sistema aplica los cambios sin requerir permiso adicional de "Ana"
```

**Supuestos / preguntas abiertas**
- Se asume que todos los administradores tienen el mismo nivel de permisos entre sí (no hay jerarquía ni administrador "dueño" con más control que otros), ya que el requerimiento dice "cualquier administrador".
- No se especifica si se debe mostrar quién es el creador original de la ficha al resto de administradores; se asume que sí, como dato informativo, aunque no como restricción de permisos.

**Mockup**

![Mockup HU-15](./mockups/HU-15.png)

Prompt sugerido (Lovable / v0.dev):
> Panel de administración con un listado de todas las fichas del atlas (propias y de otros administradores), cada fila con nombre del ejercicio, estado (borrador/publicada/archivada), nombre del administrador creador, y acciones de ver/editar disponibles para cualquier administrador sin importar quién la creó.
