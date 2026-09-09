### HU-32: Ver la métrica de ejercicios más consultados

**Origen:** RF_057

**Como** administrador
**Quiero** ver cuáles son los ejercicios más consultados por los usuarios
**Para** identificar qué contenido genera más interés y priorizar su mantenimiento o mejora

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista el panel de métricas (HU-30) como contenedor y de que existan fichas consultadas (HU-02).
- N — Negotiable: ✅ No impone el diseño de la visualización (lista, gráfico de barras, etc.).
- V — Valuable: ✅ Ayuda a priorizar qué contenido revisar, actualizar o promocionar.
- E — Estimable: ✅ Alcance acotado a un ranking basado en el conteo de visualizaciones de ficha.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que el orden mostrado corresponde al número de veces que se vio cada ficha.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Métrica de ejercicios más consultados

  Escenario: Consulta del ranking de ejercicios más vistos
    Dado que existen fichas de ejercicio con distintas cantidades de visualizaciones registradas
    Cuando el administrador consulta esta métrica en el panel
    Entonces el sistema muestra los ejercicios ordenados de mayor a menor número de visualizaciones

  Escenario: No hay suficientes ejercicios consultados para completar el ranking
    Dado que existen menos ejercicios con visualizaciones registradas que el tamaño del ranking
    Cuando el administrador consulta esta métrica
    Entonces el sistema muestra únicamente los ejercicios que sí tienen visualizaciones registradas
```

**Supuestos / preguntas abiertas**
- No se especifica cuántos ejercicios muestra el ranking; se asumió un top 10, a confirmar con el equipo.
- Se asume que cada visualización de la ficha completa de un ejercicio (HU-02) cuenta como una consulta para este ranking.

**Mockup**

![Mockup HU-32](./mockups/HU-32.png)

Prompt sugerido (Lovable / v0.dev):
> Sección "Ejercicios más consultados" del panel de métricas: lista ordenada (top 10) con el nombre del ejercicio, su región/categoría y el número de visualizaciones, representada también como un gráfico de barras horizontales.
