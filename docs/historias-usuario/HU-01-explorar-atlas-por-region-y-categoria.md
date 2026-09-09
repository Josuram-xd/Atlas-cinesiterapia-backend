### HU-01: Explorar el atlas por región anatómica y categoría terapéutica

**Origen:** RF_001, RF_002, RF_003

**Como** usuario del atlas (estudiante, profesional, practicante o visitante con sesión iniciada)
**Quiero** seleccionar una región anatómica en el cuerpo humano (vista anterior o posterior) y luego una categoría terapéutica
**Para** llegar rápidamente al listado de ejercicios relevantes para la zona y el tipo de tratamiento que necesito

**Validación INVEST**
- I — Independent: ✅ El flujo de navegación (UI de selección) se puede construir y probar de forma independiente con datos de ejemplo, sin esperar a que existan todas las fichas reales.
- N — Negotiable: ✅ Describe el qué (elegir región → ver categorías → ver ejercicios) sin imponer la solución técnica (tipo de mapa corporal, librería de UI, etc.).
- V — Valuable: ✅ Es el mecanismo principal de descubrimiento de contenido del atlas; sin esto el usuario no puede llegar a los ejercicios.
- E — Estimable: ✅ Alcance acotado a tres pantallas/estados de navegación encadenados.
- S — Small: ✅ Cabe en una iteración corta (selección de región → menú de categorías → listado).
- T — Testable: ✅ Cada paso tiene un resultado observable y verificable (qué se muestra tras cada selección).

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Exploración del atlas por región anatómica y categoría terapéutica

  Escenario: Selección de una región anatómica muestra las categorías terapéuticas
    Dado que el usuario tiene sesión iniciada y está en la pantalla principal del atlas
    Y el sistema muestra el cuerpo humano en vista anterior y posterior
    Cuando el usuario selecciona una región anatómica
    Entonces el sistema muestra el menú de las 9 categorías terapéuticas disponibles para esa región

  Escenario: Selección de una categoría terapéutica muestra el listado de ejercicios
    Dado que el usuario ya seleccionó una región anatómica y ve el menú de categorías terapéuticas
    Cuando el usuario selecciona una categoría terapéutica
    Entonces el sistema muestra el listado de nombres de los ejercicios publicados para esa región y categoría

  Escenario: Cambio entre vista anterior y posterior del cuerpo humano
    Dado que el usuario está en la pantalla principal del atlas
    Cuando el usuario cambia entre la vista anterior y la vista posterior del cuerpo humano
    Entonces el sistema muestra las regiones anatómicas correspondientes a la vista seleccionada

  Escenario: Categoría terapéutica sin ejercicios publicados
    Dado que el usuario seleccionó una región y una categoría terapéutica
    Y no existen ejercicios publicados para esa combinación de región y categoría
    Cuando el sistema muestra el listado de ejercicios
    Entonces el sistema muestra un mensaje indicando que no hay ejercicios disponibles para esa selección
```

**Supuestos / preguntas abiertas**
- El requerimiento no especifica el mensaje exacto para el caso "sin ejercicios disponibles"; se asumió un mensaje genérico de estado vacío.
- Se asume que las 9 categorías terapéuticas son las mismas para todas las regiones anatómicas (el requerimiento no indica que varíen por región).
- RF_022 y RF_023 (una ficha pertenece a una única región y una única categoría) no se documentan como historias propias: son reglas de integridad de datos que ya quedan cubiertas como precondición de esta historia (el listado que se muestra depende de esa relación 1—N) y se validan formalmente en la historia de creación de la ficha (módulo "Gestión de la ficha de ejercicio").

**Mockup**

![Mockup HU-01](./mockups/HU-01.png)

Prompt sugerido (Lovable / v0.dev):
> Pantalla principal de un atlas de ejercicios de fisioterapia: silueta de cuerpo humano interactiva con toggle anterior/posterior, regiones resaltables al pasar el mouse y barra de búsqueda superior. Al seleccionar una región, se abre un panel con 9 categorías terapéuticas en tarjetas (breadcrumb "Región > Categorías"). Al elegir una categoría, se muestra un listado en grid con los nombres de los ejercicios disponibles, con estado vacío ilustrado cuando no hay resultados.
