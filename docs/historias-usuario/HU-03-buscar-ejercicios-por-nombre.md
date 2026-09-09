### HU-03: Buscar ejercicios por nombre

**Origen:** RF_051

**Como** usuario del atlas
**Quiero** buscar ejercicios escribiendo su nombre o parte de él
**Para** encontrar un ejercicio específico sin tener que navegar por región y categoría

**Validación INVEST**
- I — Independent: ✅ Es una vía de acceso alternativa a la navegación por región/categoría (HU-01); se puede entregar por separado.
- N — Negotiable: ✅ No impone mecanismo de búsqueda (coincidencia exacta, parcial, tolerante a tildes, etc.), eso queda abierto a diseño.
- V — Valuable: ✅ Reduce el tiempo para encontrar un ejercicio conocido por nombre.
- E — Estimable: ✅ Alcance acotado a un campo de búsqueda y una lista de resultados.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar qué resultados devuelve la búsqueda para una entrada dada.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Búsqueda de ejercicios por nombre

  Escenario: Búsqueda con coincidencias
    Dado que existen ejercicios publicados cuyo nombre contiene el texto "sentadilla"
    Cuando el usuario busca "sentadilla"
    Entonces el sistema muestra el listado de ejercicios publicados cuyo nombre contiene "sentadilla"

  Escenario: Búsqueda sin coincidencias
    Dado que ningún ejercicio publicado tiene un nombre que coincida con el texto buscado
    Cuando el usuario realiza la búsqueda
    Entonces el sistema muestra un mensaje indicando que no se encontraron ejercicios para esa búsqueda

  Escenario: Búsqueda con texto vacío
    Dado que el usuario está en el campo de búsqueda
    Cuando el usuario intenta buscar sin ingresar ningún texto
    Entonces el sistema no ejecuta la búsqueda y solicita ingresar un texto para buscar
```

**Supuestos / preguntas abiertas**
- No se especifica si la búsqueda debe ser insensible a mayúsculas/minúsculas y tildes, ni si aplica coincidencia parcial o exacta; se asumió coincidencia parcial e insensible a mayúsculas/tildes por ser el comportamiento estándar esperado por un usuario final.
- No se especifica un mínimo de caracteres para disparar la búsqueda; se asumió que solo se bloquea el texto completamente vacío.
- La búsqueda solo considera ejercicios con ficha publicada (coherente con HU-02).

**Mockup**

![Mockup HU-03](./mockups/HU-03.png)

Prompt sugerido (Lovable / v0.dev):
> Campo de búsqueda en la cabecera del atlas de ejercicios de fisioterapia. Al escribir, se muestra debajo un listado en grid con los ejercicios cuyo nombre coincide, o un estado vacío ilustrado con el mensaje "no se encontraron ejercicios" cuando no hay coincidencias.
