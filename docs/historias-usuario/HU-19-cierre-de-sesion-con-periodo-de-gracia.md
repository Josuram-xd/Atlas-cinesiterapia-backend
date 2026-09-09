### HU-19: Cierre automático de sesión con periodo de gracia

**Origen:** RF_029, RF_030

**Como** usuario del atlas
**Quiero** que mi sesión se cierre automáticamente al cerrar la página, pero con un margen de tolerancia ante cierres accidentales
**Para** que mi cuenta quede protegida cuando realmente me voy, sin perder la sesión por un cierre accidental de la pestaña

**Validación INVEST**
- I — Independent: ✅ Es un comportamiento transversal de la sesión, no depende de otra historia funcional.
- N — Negotiable: ✅ No impone el mecanismo técnico (evento del navegador, temporizador de servidor, etc.), solo el comportamiento observable.
- V — Valuable: ✅ Protege cuentas en dispositivos compartidos y evita cierres de sesión molestos por accidentes.
- E — Estimable: ✅ Regla de tiempo acotada y conocida (un minuto de gracia).
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar el estado de la sesión antes y después del minuto de gracia.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Cierre automático de sesión con periodo de gracia

  Escenario: Cierre de sesión tras cerrar la página y agotarse el periodo de gracia
    Dado que el usuario tiene una sesión iniciada
    Cuando cierra la página o pestaña del atlas y transcurre más de un minuto sin volver a abrirla
    Entonces el sistema invalida la sesión

  Escenario: La sesión se mantiene si el usuario vuelve a abrir la página dentro del minuto de gracia
    Dado que el usuario cerró accidentalmente la página del atlas
    Cuando vuelve a abrir el atlas antes de que transcurra un minuto
    Entonces el sistema mantiene su sesión iniciada sin pedirle iniciar sesión de nuevo

  Escenario: Cierre de sesión inmediato al cerrar sesión manualmente
    Dado que el usuario tiene una sesión iniciada
    Cuando usa la opción de cerrar sesión de forma explícita
    Entonces el sistema invalida la sesión de inmediato, sin esperar el periodo de gracia
```

**Supuestos / preguntas abiertas**
- No se especifica si el periodo de gracia aplica solo al cierre de la pestaña o también al cierre completo del navegador; se asumió que aplica a ambos casos por igual.
- No se especifica si se debe mostrar algún indicador visual del periodo de gracia al usuario; se asumió que el comportamiento es transparente (no requiere una notificación explícita).

**Mockup**

![Mockup HU-19](./mockups/HU-19.png)

Prompt sugerido (Lovable / v0.dev):
> Pantalla de sesión expirada: al reabrir el atlas después del periodo de gracia, se muestra un mensaje "Tu sesión expiró, vuelve a iniciar sesión" con un botón que lleva al formulario de inicio de sesión.
