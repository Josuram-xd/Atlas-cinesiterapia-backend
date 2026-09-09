### HU-12: Editar una ficha publicada

**Origen:** RF_027

**Como** administrador (profesora)
**Quiero** editar el contenido de una ficha que ya está publicada
**Para** corregir o actualizar información clínica sin tener que despublicarla ni crear una ficha nueva

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista una ficha publicada (HU-11).
- N — Negotiable: ✅ No impone si la edición se hace en el mismo formulario de creación o en uno dedicado.
- V — Valuable: ✅ Permite mantener el contenido clínico actualizado y corregir errores sin fricción.
- E — Estimable: ✅ Alcance acotado: reutiliza los campos ya definidos en el Módulo 2.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que los cambios quedan reflejados en la ficha publicada.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Edición de una ficha publicada

  Escenario: Edición exitosa de una ficha publicada
    Dado que existe una ficha en estado publicado
    Cuando el administrador modifica uno o más de sus campos y guarda los cambios
    Entonces el sistema actualiza la ficha publicada con los nuevos valores
    Y la ficha permanece visible para los estudiantes durante y después de la edición

  Escenario: La edición no puede dejar la ficha sin campos obligatorios
    Dado que el administrador está editando una ficha publicada
    Cuando intenta guardar los cambios habiendo vaciado un campo obligatorio
    Entonces el sistema impide guardar y muestra el campo obligatorio pendiente
```

**Supuestos / preguntas abiertas**
- Se asume que los cambios se aplican de inmediato sobre la ficha publicada, sin pasar de nuevo por un estado de borrador ni requerir una segunda aprobación, ya que el requerimiento no indica un flujo de re-revisión.
- Se reutiliza la misma regla de campos obligatorios definida en HU-11 para impedir que una edición deje la ficha incompleta.

**Mockup**

![Mockup HU-12](./mockups/HU-12.png)

Prompt sugerido (Lovable / v0.dev):
> Vista de edición de una ficha ya publicada: mismo formulario de creación de ficha, precargado con los datos actuales, con un aviso superior "Estás editando una ficha publicada — los cambios se verán de inmediato" y botón "Guardar cambios".
