### HU-31: Ver la métrica de visitas totales por periodo

**Origen:** RF_032, RF_056

**Como** administrador
**Quiero** ver el total de visitas al atlas agrupadas por periodo de tiempo
**Para** entender cuánto se está usando el atlas y detectar tendencias de uso

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista el panel de métricas (HU-30) como contenedor.
- N — Negotiable: ✅ No impone el tipo de gráfico ni los periodos exactos disponibles.
- V — Valuable: ✅ Es un indicador clave de adopción del atlas.
- E — Estimable: ✅ Alcance acotado a un conteo agregado por periodo.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que el total mostrado corresponde a las visitas registradas en el periodo elegido.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Métrica de visitas totales por periodo

  Escenario: Registro de una visita al atlas
    Dado que un usuario con sesión iniciada navega a cualquier página del atlas
    Cuando esa visita ocurre
    Entonces el sistema la registra con la fecha y el usuario correspondiente

  Escenario: Consulta del total de visitas de un periodo
    Dado que existen visitas registradas en distintas fechas
    Cuando el administrador selecciona un periodo de tiempo en el panel de métricas
    Entonces el sistema muestra el total de visitas registradas dentro de ese periodo

  Escenario: Periodo sin visitas registradas
    Dado que no hay visitas registradas dentro de un periodo seleccionado
    Cuando el administrador consulta ese periodo
    Entonces el sistema muestra un total de cero visitas para ese periodo
```

**Supuestos / preguntas abiertas**
- RF_032 (registro de cada visita con fecha y usuario) no se documenta como historia propia: es la captura de datos que alimenta esta métrica, así que queda cubierta aquí como precondición.
- No se especifican los periodos exactos disponibles para agrupar; se asumieron periodos estándar (día, semana, mes) con posibilidad de elegir un rango de fechas, a confirmar con el equipo.
- No se especifica qué cuenta como "una visita" (por ejemplo, cada carga de página o solo el inicio de una sesión); se asumió que se registra cada vez que un usuario navega a una página del atlas durante su sesión.

**Mockup**

![Mockup HU-31](./mockups/HU-31.png)

Prompt sugerido (Lovable / v0.dev):
> Sección "Visitas por periodo" del panel de métricas: selector de periodo (día/semana/mes/rango personalizado) y un gráfico de líneas o barras mostrando el total de visitas a lo largo del tiempo, con el número total destacado arriba.
