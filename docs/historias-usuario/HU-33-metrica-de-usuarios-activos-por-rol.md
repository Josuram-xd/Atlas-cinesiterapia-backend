### HU-33: Ver la métrica de usuarios activos por rol

**Origen:** RF_058

**Como** administrador
**Quiero** ver el número de usuarios activos agrupados por rol
**Para** entender qué tipo de público está usando realmente el atlas (estudiantes, profesionales, practicantes o visitantes)

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista el panel de métricas (HU-30) y de que los usuarios tengan un rol asignado (HU-17).
- N — Negotiable: ✅ No impone el tipo de visualización, solo que se agrupe por rol.
- V — Valuable: ✅ Permite entender la composición real de la audiencia del atlas.
- E — Estimable: ✅ Alcance acotado a un conteo agrupado por los 4 roles ya definidos.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que el conteo por rol corresponde a los usuarios que cumplen la definición de "activo".

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Métrica de usuarios activos por rol

  Escenario: Consulta del número de usuarios activos por rol
    Dado que existen usuarios registrados con los roles Estudiante, Profesional,
      Practicante y Visitante/curioso, algunos de ellos activos en los últimos 30 días
    Cuando el administrador consulta esta métrica en el panel
    Entonces el sistema muestra el número de usuarios activos agrupado por cada uno de los 4 roles

  Escenario: Un rol sin usuarios activos
    Dado que ningún usuario de un rol determinado ha estado activo en los últimos 30 días
    Cuando el administrador consulta esta métrica
    Entonces el sistema muestra ese rol con un total de cero usuarios activos
```

**Supuestos / preguntas abiertas**
- El requerimiento no define qué significa "usuario activo"; se asumió que es un usuario que inició sesión al menos una vez en los últimos 30 días. Este es un supuesto importante a confirmar con el equipo, ya que cambia directamente el número mostrado.

**Mockup**

![Mockup HU-33](./mockups/HU-33.png)

Prompt sugerido (Lovable / v0.dev):
> Sección "Usuarios activos por rol" del panel de métricas: gráfico de barras o de dona con los 4 roles (Estudiante, Profesional, Practicante, Visitante/curioso) y el número de usuarios activos de cada uno, con el total general destacado arriba.
