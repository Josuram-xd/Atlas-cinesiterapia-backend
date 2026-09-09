### HU-10: Guardar la ficha como borrador

**Origen:** RF_024

**Como** administrador
**Quiero** guardar mi avance en una ficha de ejercicio sin publicarla
**Para** poder completar el contenido de la ficha en varias sesiones de trabajo sin que los estudiantes la vean antes de tiempo

**Validación INVEST**
- I — Independent: ⚠️ Es el estado natural de toda ficha nueva creada en el Módulo 2 (HU-04 a HU-09); se puede probar de forma aislada con cualquier subconjunto de campos ya completado.
- N — Negotiable: ✅ No impone el mecanismo de guardado (autoguardado, botón explícito, etc.), solo el resultado: la ficha no debe ser visible para los estudiantes.
- V — Valuable: ✅ Evita perder trabajo y evita exponer contenido incompleto o no revisado a los estudiantes.
- E — Estimable: ✅ Alcance acotado: un estado "borrador" y su regla de visibilidad.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que una ficha en borrador no aparece en el atlas para los usuarios.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Guardado de la ficha como borrador

  Escenario: Guardado de una ficha incompleta como borrador
    Dado que el administrador está creando o editando una ficha de ejercicio
    Y no ha completado todos los campos obligatorios
    Cuando guarda la ficha
    Entonces el sistema almacena la ficha en estado borrador con los campos ya ingresados

  Escenario: Una ficha en borrador no es visible para los estudiantes
    Dado que existe una ficha guardada en estado borrador
    Cuando un usuario navega por el atlas o busca ejercicios
    Entonces esa ficha no aparece en ningún listado ni es accesible desde el atlas

  Escenario: Reanudación de la edición de un borrador
    Dado que el administrador tiene una ficha guardada en estado borrador
    Cuando vuelve a abrirla para seguir completándola
    Entonces el sistema muestra los campos previamente guardados listos para editar
```

**Supuestos / preguntas abiertas**
- No se especifica si el guardado como borrador es automático (autoguardado) o requiere una acción explícita del administrador; se asumió una acción explícita ("Guardar borrador"), por ser lo más fácil de hacer testeable y predecible.

**Mockup**

![Mockup HU-10](./mockups/HU-10.png)

Prompt sugerido (Lovable / v0.dev):
> Barra de acciones fija en la parte inferior del formulario de ficha clínica, con un botón secundario "Guardar borrador" y una etiqueta de estado "Borrador · no visible para estudiantes" junto al nombre de la ficha.
