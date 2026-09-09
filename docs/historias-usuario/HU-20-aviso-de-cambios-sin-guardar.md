### HU-20: Aviso de cambios sin guardar al cerrar la página

**Origen:** RF_031

**Como** usuario que está editando contenido en el atlas
**Quiero** recibir una advertencia si intento cerrar la página teniendo cambios sin guardar
**Para** evitar perder mi trabajo por cerrar accidentalmente la pestaña o el navegador

**Validación INVEST**
- I — Independent: ✅ Es un comportamiento transversal de la interfaz, no depende de otra historia funcional específica.
- N — Negotiable: ✅ No impone el mecanismo (diálogo nativo del navegador, modal propio, etc.).
- V — Valuable: ✅ Evita la pérdida de trabajo, especialmente en formularios largos como los de la ficha de ejercicio.
- E — Estimable: ✅ Comportamiento acotado: detectar cambios sin guardar y advertir antes de salir.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar si aparece o no la advertencia según haya o no cambios pendientes.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Aviso de cambios sin guardar

  Escenario: Advertencia al cerrar con cambios sin guardar
    Dado que el usuario modificó campos de un formulario sin guardar los cambios
    Cuando intenta cerrar la página o la pestaña
    Entonces el sistema muestra una advertencia preguntando si desea salir sin guardar

  Escenario: Cierre sin advertencia cuando no hay cambios pendientes
    Dado que el usuario no ha modificado ningún campo, o ya guardó sus cambios
    Cuando intenta cerrar la página o la pestaña
    Entonces el sistema la cierra sin mostrar ninguna advertencia

  Escenario: El usuario decide salir descartando los cambios
    Dado que el sistema muestra la advertencia de cambios sin guardar
    Cuando el usuario confirma que quiere salir de todas formas
    Entonces el sistema cierra la página y los cambios sin guardar se pierden
```

**Supuestos / preguntas abiertas**
- El requerimiento no delimita a qué formularios aplica; se asumió que aplica principalmente a los formularios de la ficha de ejercicio (Módulo 2) y que puede extenderse a otros formularios con edición de texto, como los comentarios.

**Mockup**

![Mockup HU-20](./mockups/HU-20.png)

Prompt sugerido (Lovable / v0.dev):
> Diálogo de confirmación al intentar cerrar la pestaña con cambios sin guardar en el formulario de ficha: mensaje "Tienes cambios sin guardar. ¿Seguro que quieres salir?" con opciones "Salir sin guardar" y "Seguir editando".
