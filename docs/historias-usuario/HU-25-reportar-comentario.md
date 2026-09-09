### HU-25: Reportar un comentario ofensivo

**Origen:** RF_039

**Como** usuario del atlas
**Quiero** reportar el comentario de otro usuario que considero ofensivo
**Para** pedirle a un administrador que lo revise, aunque el filtro automático no lo haya detectado

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista un comentario publicado por otro usuario (HU-21).
- N — Negotiable: ✅ No impone el mecanismo de reporte (botón, menú, motivo obligatorio, etc.).
- V — Valuable: ✅ Complementa el filtro automático con la moderación de la propia comunidad.
- E — Estimable: ✅ Acción acotada: marcar un comentario para revisión administrativa.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que un comentario reportado llega a la página de revisión del administrador.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Reporte de comentarios por usuarios

  Escenario: Reporte exitoso de un comentario de otro usuario
    Dado que el usuario "luis@correo.com" ve un comentario publicado por otro usuario
    Cuando lo reporta como ofensivo
    Entonces el sistema registra el reporte y el comentario queda disponible en la
      página de revisión de comentarios del administrador

  Escenario: Un comentario reportado permanece visible mientras se revisa
    Dado que un comentario fue reportado por un usuario
    Cuando otros usuarios consultan la ficha del ejercicio
    Entonces el comentario reportado se sigue mostrando con normalidad hasta que un
      administrador lo revise

  Escenario: Un usuario no puede reportar el mismo comentario más de una vez
    Dado que el usuario "luis@correo.com" ya reportó un comentario
    Cuando intenta reportar el mismo comentario de nuevo
    Entonces el sistema no genera un segundo reporte de ese usuario sobre ese comentario
```

**Supuestos / preguntas abiertas**
- Se interpretó que reportar un comentario no lo oculta automáticamente (a diferencia del filtro de RF_037): el comentario reportado sigue visible para todos hasta que un administrador lo revise en HU-27, ya que RF_039 no menciona ocultamiento, solo el filtro automático (RF_037) lo hace.
- No se especifica si un usuario puede reportar su propio comentario, ni si se le pide un motivo al reportar; se asumió que solo puede reportar comentarios ajenos y que no se exige un motivo obligatorio.

**Mockup**

![Mockup HU-25](./mockups/HU-25.png)

Prompt sugerido (Lovable / v0.dev):
> Menú de opciones (ícono de tres puntos) en un comentario ajeno, con la opción "Reportar comentario". Al seleccionarla, aparece una confirmación breve "Comentario reportado, un administrador lo revisará".
