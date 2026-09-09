### HU-08: Cargar el video e imágenes del ejercicio

**Origen:** RF_017, RF_018

**Como** administrador
**Quiero** cargar un mini video del ejercicio y una galería de imágenes de la posición inicial, final y de las tomas del terapeuta
**Para** que el usuario pueda ver visualmente cómo se ejecuta el ejercicio, además de leer su descripción

**Validación INVEST**
- I — Independent: ⚠️ Depende de que exista el registro base de la ficha (HU-04); el desarrollo de esta sección puede avanzar en paralelo con datos de prueba.
- N — Negotiable: ✅ No impone el reproductor de video ni el componente de galería a usar, solo los formatos soportados y el contenido esperado.
- V — Valuable: ✅ El material visual es lo que más facilita reproducir el ejercicio correctamente.
- E — Estimable: ✅ Alcance acotado a dos tipos de carga de archivos con formatos definidos.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar qué formatos se aceptan/rechazan y que el video y las imágenes se reproducen/visualizan correctamente.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Multimedia de la ficha de ejercicio

  Escenario: Carga de un mini video en formato soportado
    Dado que existe una ficha con su información general ya registrada
    Cuando el administrador carga un archivo de video en formato MP4, WebM o GIF
    Entonces el sistema guarda el video y permite reproducirlo desde la ficha

  Escenario: Intento de carga de un video en formato no soportado
    Dado que el administrador está cargando el video de una ficha
    Cuando intenta cargar un archivo que no está en formato MP4, WebM o GIF
    Entonces el sistema rechaza el archivo y muestra un mensaje indicando los formatos soportados

  Escenario: Carga de la galería de imágenes
    Dado que existe una ficha con su información general ya registrada
    Cuando el administrador carga imágenes de la posición inicial, la posición final
      y las tomas del terapeuta
    Entonces el sistema guarda las imágenes y las muestra en la galería de la ficha
```

**Supuestos / preguntas abiertas**
- No se especifican los formatos válidos para las imágenes de la galería; se asumieron formatos estándar de imagen web (JPG, PNG, WebP).
- No se especifica un tamaño máximo de archivo ni una duración máxima para el mini video; queda como punto a definir con el equipo antes de implementar la validación de carga.
- No se especifica si la galería admite más de una imagen por categoría (inicial/final/tomas del terapeuta); se asumió que sí, sin límite explícito.

**Mockup**

![Mockup HU-08](./mockups/HU-08.png)

Prompt sugerido (Lovable / v0.dev):
> Formulario de ficha clínica, sección "Multimedia": zona de carga (drag and drop) para un mini video con formatos aceptados MP4/WebM/GIF indicados junto al control, y una galería de carga de imágenes agrupada en tres categorías: posición inicial, posición final y tomas del fisioterapeuta, con miniaturas de previsualización.
