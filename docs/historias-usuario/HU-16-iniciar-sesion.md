### HU-16: Iniciar sesión en el atlas

**Origen:** RF_028

**Como** usuario registrado del atlas (estudiante, profesional, practicante, visitante o administrador)
**Quiero** iniciar sesión con mi correo y contraseña
**Para** poder ver y usar el atlas, ya que el acceso requiere tener una sesión iniciada

**Validación INVEST**
- I — Independent: ✅ Es la puerta de entrada al sistema; no depende de ninguna otra historia funcional.
- N — Negotiable: ✅ No impone el mecanismo de autenticación interno, solo que el acceso se haga con correo y contraseña.
- V — Valuable: ✅ Sin inicio de sesión no hay acceso a ninguna otra funcionalidad del atlas.
- E — Estimable: ✅ Alcance acotado a un formulario de login y sus validaciones.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar el acceso con credenciales válidas e inválidas.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Inicio de sesión

  Escenario: Inicio de sesión exitoso
    Dado que el usuario "ana@correo.com" está registrado en el sistema
    Cuando ingresa su correo y su contraseña correcta
    Entonces el sistema inicia sesión y le da acceso al atlas

  Escenario: Intento de inicio de sesión con contraseña incorrecta
    Dado que el usuario "ana@correo.com" está registrado en el sistema
    Cuando ingresa su correo con una contraseña incorrecta
    Entonces el sistema no inicia sesión y muestra un mensaje de credenciales inválidas

  Escenario: Acceso bloqueado sin sesión iniciada
    Dado que un usuario no tiene sesión iniciada
    Cuando intenta acceder a cualquier pantalla del atlas
    Entonces el sistema lo redirige a la pantalla de inicio de sesión
```

**Supuestos / preguntas abiertas**
- No se especifica un número máximo de intentos fallidos ni un bloqueo temporal de cuenta; se asumió que no hay límite por ahora, a confirmar considerando RNF_002 (protección de credenciales).
- El inicio de sesión exitoso es el punto de partida del acceso sin conexión descrito en RNF_004 (módulo "Requerimientos no funcionales"): el contenido se cachea localmente a partir de aquí.

**Mockup**

![Mockup HU-16](./mockups/HU-16.png)

Prompt sugerido (Lovable / v0.dev):
> Pantalla de inicio de sesión de un atlas clínico: campo de correo electrónico, campo de contraseña, botón "Iniciar sesión", enlace "¿Olvidaste tu contraseña?" y enlace "Crear cuenta" para quienes no tienen una.
