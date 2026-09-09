### HU-18: Recuperar contraseña olvidada

**Origen:** RF_047

**Como** usuario registrado que olvidó su contraseña
**Quiero** solicitar un restablecimiento de mi contraseña ingresando mi correo
**Para** recuperar el acceso a mi cuenta sin depender de un administrador

**Validación INVEST**
- I — Independent: ✅ Se puede entregar y probar por separado del flujo de inicio de sesión (HU-16).
- N — Negotiable: ✅ No impone el mecanismo exacto (enlace o código), solo el resultado esperado.
- V — Valuable: ✅ Evita que un usuario quede permanentemente bloqueado fuera de su cuenta.
- E — Estimable: ✅ Alcance acotado a solicitud de recuperación y definición de nueva contraseña.
- S — Small: ✅ Cabe en una iteración corta.
- T — Testable: ✅ Se puede verificar el envío de la recuperación y el cambio efectivo de la contraseña.

**Criterios de aceptación (Gherkin)**
```gherkin
# language: es
Característica: Recuperación de contraseña

  Escenario: Solicitud de recuperación con correo registrado
    Dado que el usuario "ana@correo.com" está registrado en el sistema
    Cuando solicita la recuperación de su contraseña ingresando ese correo
    Entonces el sistema envía un enlace de restablecimiento a ese correo

  Escenario: Solicitud de recuperación con correo no registrado
    Dado que el correo "noexiste@correo.com" no está registrado en el sistema
    Cuando se solicita la recuperación de contraseña con ese correo
    Entonces el sistema muestra el mismo mensaje genérico de confirmación, sin revelar
      si el correo existe o no en el sistema

  Escenario: Definición de una nueva contraseña
    Dado que el usuario recibió un enlace de restablecimiento válido
    Cuando ingresa y confirma una nueva contraseña a través de ese enlace
    Entonces el sistema actualiza la contraseña y le permite iniciar sesión con la nueva
```

**Supuestos / preguntas abiertas**
- No se especifica el mecanismo exacto de recuperación (enlace por correo, código de un solo uso, etc.) ni su tiempo de expiración; se asumió un enlace enviado por correo, sin fijar un tiempo de expiración concreto, a definir con el equipo.
- Se asumió un mensaje genérico para el caso de correo no registrado como buena práctica de seguridad (evitar revelar qué correos existen), en línea con RNF_002, aunque el requerimiento no lo especifica.

**Mockup**

![Mockup HU-18](./mockups/HU-18.png)

Prompt sugerido (Lovable / v0.dev):
> Flujo de recuperación de contraseña en dos pantallas: (1) formulario con campo de correo y botón "Enviar enlace de recuperación" más un mensaje de confirmación genérico; (2) formulario de nueva contraseña con campos "Nueva contraseña" y "Confirmar contraseña", accesible desde el enlace recibido.
