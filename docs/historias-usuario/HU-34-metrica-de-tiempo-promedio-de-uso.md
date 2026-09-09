### HU-34: Ver la métrica de tiempo promedio de uso por sesión

**Origen:** RF_059

**Como** administrador
**Quiero** ver el tiempo promedio de uso del atlas por sesión
**Para** entender qué tan a fondo se está consultando el atlas en cada visita

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista el panel de métricas (HU-30) y del mecanismo de sesión con cierre y periodo de gracia (HU-19), que determina cuándo termina una sesión.
- N — Negotiable: ✅ No impone cómo se calcula internamente el promedio, solo el dato que debe mostrarse.
- V — Valuable: ✅ Complementa las visitas totales (HU-31) con una medida de profundidad de uso.
- E — Estimable: ✅ Alcance acotado a un promedio calculado sobre la duración de las sesiones.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que el promedio mostrado corresponde a la duración registrada de las sesiones.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Métrica de tiempo promedio de uso por sesión

  Escenario: Consulta del tiempo promedio de uso
    Dado que existen sesiones de distintos usuarios con su duración registrada
    Cuando el administrador consulta esta métrica en el panel
    Entonces el sistema muestra el tiempo promedio de duración de esas sesiones

  Escenario: No hay sesiones registradas para calcular el promedio
    Dado que no existen sesiones con duración registrada
    Cuando el administrador consulta esta métrica
    Entonces el sistema muestra que no hay datos suficientes para calcular el promedio
```

**Supuestos / preguntas abiertas**
- Se asume que la duración de una sesión se mide desde el inicio de sesión (HU-16) hasta su cierre, ya sea manual o automático por el periodo de gracia (HU-19).
- No se especifica sobre qué periodo se calcula el promedio (histórico total, últimos 30 días, etc.); se asumió que se puede filtrar por el mismo periodo seleccionado en la métrica de visitas (HU-31), para mantener consistencia en el panel.

**Mockup**

![Mockup HU-34](./mockups/HU-34.png)

Prompt sugerido (Lovable / v0.dev):
> Sección "Tiempo promedio de uso" del panel de métricas: tarjeta destacada tipo KPI mostrando el tiempo promedio por sesión (ej. "12 min"), con un indicador de tendencia respecto al periodo anterior.
