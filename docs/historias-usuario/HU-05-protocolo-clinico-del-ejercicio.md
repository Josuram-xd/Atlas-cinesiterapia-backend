### HU-05: Registrar el protocolo clínico del ejercicio

**Origen:** RF_006, RF_007, RF_008, RF_009

**Como** administrador
**Quiero** registrar el objetivo terapéutico, las estructuras intervenidas, las indicaciones clínicas y las precauciones/contraindicaciones del ejercicio
**Para** documentar el criterio clínico que justifica cuándo y por qué usar el ejercicio

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista el registro base de la ficha (HU-04) para asociar estos campos; el desarrollo de esta sección puede avanzar en paralelo con datos de prueba.
- N — Negotiable: ✅ No impone el tipo de control (texto libre, listas, etiquetas), solo qué información debe quedar registrada.
- V — Valuable: ✅ Es la base clínica que justifica el uso del ejercicio; sin ella la ficha no tiene valor médico-legal.
- E — Estimable: ✅ Cuatro campos de texto con un propósito clínico claro y acotado.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que cada campo quedó registrado y se muestra correctamente al consultar la ficha.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Protocolo clínico del ejercicio

  Escenario: Registro del protocolo clínico completo
    Dado que existe una ficha con su información general ya registrada
    Cuando el administrador ingresa el objetivo terapéutico, las estructuras intervenidas
      (músculos, articulaciones y ligamentos/tejidos blandos), las indicaciones clínicas
      y las precauciones y contraindicaciones
    Entonces el sistema guarda los cuatro campos asociados a esa ficha

  Escenario: Edición del protocolo clínico de una ficha existente
    Dado que una ficha ya tiene registrado su protocolo clínico
    Cuando el administrador modifica el objetivo terapéutico, las estructuras intervenidas,
      las indicaciones o las precauciones y contraindicaciones
    Entonces el sistema actualiza la ficha con los nuevos valores
```

**Supuestos / preguntas abiertas**
- No se especifica un formato estructurado para "estructuras intervenidas" (por ejemplo, listas separadas para músculos, articulaciones y tejidos blandos, o un único campo de texto); se asumió que puede documentarse como texto libre organizado por el administrador.
- No se especifica un límite de longitud para ninguno de los cuatro campos.

**Mockup**

![Mockup HU-05](./mockups/HU-05.png)

Prompt sugerido (Lovable / v0.dev):
> Formulario de ficha clínica, sección "Protocolo clínico": campo de texto para objetivo terapéutico, campo de texto (o tres subcampos) para estructuras intervenidas (músculos, articulaciones, ligamentos/tejidos blandos), campo de texto para indicaciones clínicas y campo de texto para precauciones y contraindicaciones, cada uno con su etiqueta clara.

**Mockup**

![Mockup HU-05](./mockups/HU-05.png)

Prompt sugerido (Lovable / v0.dev):
> Formulario de ficha clínica, sección "Protocolo clínico": campo de texto para objetivo terapéutico, campo de texto (o tres subcampos) para estructuras intervenidas (músculos, articulaciones, ligamentos/tejidos blandos), campo de texto para indicaciones clínicas y campo de texto para precauciones y contraindicaciones, cada uno con su etiqueta clara.
