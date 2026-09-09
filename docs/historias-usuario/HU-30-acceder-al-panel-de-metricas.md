### HU-30: Acceder al panel de métricas de uso

**Origen:** RF_055

**Como** administrador
**Quiero** acceder a una página de métricas de uso del atlas
**Para** tener un punto único donde consultar cómo se está usando el sistema

**Validación INVEST**
- I — Independent: ✅ Es el contenedor de acceso; se puede entregar antes que las métricas individuales, mostrando el panel vacío o con datos de ejemplo.
- N — Negotiable: ✅ No impone el diseño del panel, solo que debe existir y estar restringido a administradores.
- V — Valuable: ✅ Da visibilidad sobre el uso real del atlas para la toma de decisiones académicas.
- E — Estimable: ✅ Alcance acotado: una pantalla y su control de acceso.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que solo los administradores acceden a la página.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Acceso al panel de métricas de uso

  Escenario: Un administrador accede al panel de métricas
    Dado que el administrador tiene sesión iniciada
    Cuando navega a la página de métricas de uso
    Entonces el sistema le muestra el panel con las métricas disponibles

  Escenario: Un usuario sin rol de administrador no puede acceder al panel
    Dado que un usuario con rol distinto a administrador tiene sesión iniciada
    Cuando intenta acceder a la página de métricas de uso
    Entonces el sistema le niega el acceso
```

**Supuestos / preguntas abiertas**
- Ninguno adicional a los ya declarados en las historias de las métricas individuales (HU-31 a HU-34), que se muestran dentro de este panel.

**Mockup**

![Mockup HU-30](./mockups/HU-30.png)

Prompt sugerido (Lovable / v0.dev):
> Panel de administración "Métricas de uso" con un menú lateral o de pestañas hacia cuatro secciones: Visitas por periodo, Ejercicios más consultados, Usuarios activos por rol y Tiempo promedio de uso, cada una con una vista previa en tarjeta resumen en la pantalla principal del panel.
