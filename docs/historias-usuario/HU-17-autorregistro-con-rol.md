### HU-17: Crear una cuenta y seleccionar mi rol

**Origen:** RF_046, RF_054

**Como** persona interesada en usar el atlas (estudiante, profesional, practicante o visitante/curioso)
**Quiero** crear mi propia cuenta con correo y contraseña, indicando mi rol
**Para** poder acceder al atlas por mi cuenta sin depender de que un administrador me cree el usuario

**Validación INVEST**
- I — Independent: ✅ No depende de otra historia; es la vía de entrada de nuevos usuarios al sistema.
- N — Negotiable: ✅ No impone el diseño del formulario, solo los datos que debe capturar.
- V — Valuable: ✅ Permite el crecimiento autónomo de la base de usuarios del atlas.
- E — Estimable: ✅ Alcance acotado a un formulario con tres datos (correo, contraseña, rol).
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar que la cuenta se crea con el rol elegido y que se bloquean correos duplicados.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Autorregistro de usuarios con selección de rol

  Escenario: Registro exitoso de una cuenta nueva
    Dado que el correo "nuevo@correo.com" no está registrado en el sistema
    Cuando la persona completa el formulario de registro con correo, contraseña y
      selecciona su rol (Estudiante, Profesional, Practicante o Visitante/curioso)
    Entonces el sistema crea la cuenta con ese rol y le permite iniciar sesión

  Escenario: Intento de registro con un correo ya registrado
    Dado que el correo "ana@correo.com" ya está registrado en el sistema
    Cuando alguien intenta registrarse usando ese mismo correo
    Entonces el sistema rechaza el registro e indica que ese correo ya está en uso

  Escenario: Intento de registro sin seleccionar un rol
    Dado que la persona está completando el formulario de registro
    Cuando intenta enviarlo sin seleccionar ninguno de los cuatro roles disponibles
    Entonces el sistema no crea la cuenta y solicita seleccionar un rol
```

**Supuestos / preguntas abiertas**
- RF_046 menciona literalmente "estudiante", pero RF_054 ofrece cuatro roles (Estudiante, Profesional, Practicante, Visitante/curioso) para elegir al registrarse; se asumió que el autorregistro está abierto a cualquiera de los cuatro roles, y que "estudiante" en RF_046 se usa de forma genérica para "quien se registra".
- Se asumió que el rol es un campo obligatorio del registro, ya que la métrica de usuarios activos por rol (RF_058, módulo "Métricas de uso") depende de que todo usuario tenga un rol asignado.
- No se especifica un mecanismo de verificación del correo (por ejemplo, correo de confirmación) ni reglas de complejidad para la contraseña; quedan como puntos a confirmar antes de implementar, considerando RNF_002.

**Mockup**

![Mockup HU-17](./mockups/HU-17.png)

Prompt sugerido (Lovable / v0.dev):
> Pantalla de registro de cuenta: campo de correo electrónico, campo de contraseña, selector de rol con cuatro opciones en tarjetas o radio buttons (Estudiante, Profesional, Practicante, Visitante/curioso), botón "Crear cuenta" y enlace "¿Ya tienes cuenta? Inicia sesión".
