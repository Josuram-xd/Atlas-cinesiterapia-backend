### HU-04: Registrar la información general de la ficha de ejercicio

**Origen:** RF_005, RF_022, RF_023

**Como** administrador (docente/estudiante con permisos de edición)
**Quiero** registrar el nombre del ejercicio y asignarle una única región anatómica y una única categoría terapéutica
**Para** crear el registro base de la ficha sobre el que luego se completa el resto del contenido clínico

**Validación INVEST**
- I — Independent: ✅ Es la historia base: crea el registro de la ficha del que dependen las demás secciones (HU-05 a HU-09).
- N — Negotiable: ✅ No impone si la selección de región/categoría es un dropdown, un mapa clicable u otro control.
- V — Valuable: ✅ Sin este registro base no puede existir ninguna ficha ni el resto de su contenido.
- E — Estimable: ✅ Alcance acotado a tres campos (nombre, región, categoría) y sus reglas de unicidad.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que la ficha queda asociada a exactamente una región y una categoría.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Información general de la ficha de ejercicio

  Escenario: Registro de la información general de una nueva ficha
    Dado que el administrador inicia la creación de una nueva ficha de ejercicio
    Cuando ingresa el nombre del ejercicio y selecciona una región anatómica y una categoría terapéutica
    Entonces el sistema guarda la ficha asociada a esa única región anatómica y a esa única categoría terapéutica

  Escenario: Una ficha no puede asociarse a más de una región o categoría
    Dado que el administrador está registrando la información general de una ficha
    Cuando intenta seleccionar más de una región anatómica o más de una categoría terapéutica para la misma ficha
    Entonces el sistema solo permite mantener una región anatómica y una categoría terapéutica seleccionadas a la vez

  Escenario: Edición del nombre, la región o la categoría de una ficha existente
    Dado que existe una ficha con su información general ya registrada
    Cuando el administrador modifica el nombre, la región anatómica o la categoría terapéutica
    Entonces el sistema actualiza la ficha con los nuevos valores
```

**Supuestos / preguntas abiertas**
- Se asume que la región anatómica y la categoría terapéutica se seleccionan de catálogos predefinidos (las mismas regiones y las 9 categorías usadas en la navegación del Módulo 1), no como texto libre.
- No se especifica si el nombre del ejercicio debe ser único en todo el sistema; se asumió que no es obligatorio (podría repetirse en distintas regiones/categorías), ya que el requerimiento no lo indica.

**Mockup**

![Mockup HU-04](./mockups/HU-04.png)

Prompt sugerido (Lovable / v0.dev):
> Formulario de creación de ficha de ejercicio clínico, sección "Información general": campo de texto para el nombre del ejercicio, selector desplegable de región anatómica y selector desplegable de categoría terapéutica (9 opciones), con indicador de que ambos son de selección única.
