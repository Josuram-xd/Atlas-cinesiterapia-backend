### HU-21: Escribir y consultar comentarios en un ejercicio

**Origen:** RF_033, RF_036

**Como** usuario con sesión iniciada
**Quiero** escribir un comentario de texto en la ficha de un ejercicio y ver los comentarios publicados por otros usuarios
**Para** compartir mi experiencia o dudas sobre el ejercicio y aprovechar los aportes de otros usuarios

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista una ficha publicada (HU-11) sobre la cual comentar.
- N — Negotiable: ✅ No impone el diseño de la sección de comentarios, solo que se pueda escribir y consultar.
- V — Valuable: ✅ Habilita la interacción y el aprendizaje colaborativo alrededor de cada ejercicio.
- E — Estimable: ✅ Alcance acotado a escribir y listar comentarios de texto.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que un comentario publicado aparece en el listado de la ficha correspondiente.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Escritura y consulta de comentarios

  Escenario: Publicación de un comentario sin contenido ofensivo
    Dado que el usuario "carla@correo.com" tiene sesión iniciada y ve la ficha de un ejercicio
    Cuando escribe un comentario de texto sin contenido ofensivo y lo envía
    Entonces el sistema publica el comentario asociado a esa ficha y a ese usuario
    Y el comentario queda visible para cualquier usuario con sesión iniciada

  Escenario: Consulta de los comentarios publicados de un ejercicio
    Dado que un ejercicio tiene comentarios publicados de distintos usuarios
    Cuando otro usuario con sesión iniciada consulta la ficha de ese ejercicio
    Entonces el sistema le muestra todos los comentarios publicados de ese ejercicio

  Escenario: Ficha de ejercicio sin comentarios
    Dado que un ejercicio no tiene ningún comentario publicado
    Cuando un usuario consulta su ficha
    Entonces el sistema muestra un mensaje indicando que aún no hay comentarios
```

**Supuestos / preguntas abiertas**
- RF_043 (un comentario pertenece a un único ejercicio) y RF_044 (un comentario pertenece a un único usuario autor) no se documentan como historias propias: quedan cubiertas aquí, ya que cada comentario se crea siempre asociado a la ficha que se está consultando y al usuario con sesión iniciada.
- No se especifica el orden en que se muestran los comentarios; se asumió orden cronológico, del más reciente al más antiguo.
- No se especifica un límite de longitud para el comentario.
- Este criterio de aceptación no contempla aún el filtro automático de contenido ofensivo (ver HU-24) ni el estado "en revisión" (ver HU-24).

**Mockup**

![Mockup HU-21](./mockups/HU-21.png)

Prompt sugerido (Lovable / v0.dev):
> Sección de comentarios al final de la ficha de un ejercicio: campo de texto con botón "Comentar" arriba, y debajo una lista de comentarios con avatar/nombre del autor, texto y fecha, ordenados del más reciente al más antiguo, con un estado vacío ilustrado cuando no hay comentarios.
