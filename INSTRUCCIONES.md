# APK "Dar de baja · I+D" — Instrucciones de compilación

## Qué necesitas instalar (solo una vez)

1. Descarga **Android Studio** (gratis): https://developer.android.com/studio
2. Instálalo con las opciones por defecto. Incluye automáticamente el SDK de Android y Gradle.

---

## Cómo abrir y compilar el proyecto

1. Abre Android Studio
2. Haz clic en **"Open"** y selecciona la carpeta `DarDeBajaApp`
3. Android Studio mostrará un aviso "Gradle sync required" → haz clic en **"Sync Now"**
   - Descargará automáticamente todas las dependencias (~5 min la primera vez)
4. Una vez sincronizado, ve al menú: **Build → Build Bundle(s)/APK(s) → Build APK(s)**
5. Espera a que compile (1-2 minutos)
6. Android Studio mostrará un aviso: **"APK generated"** con un enlace "locate"
7. El fichero `app-debug.apk` estará en: `app/build/outputs/apk/debug/`

---

## Cómo instalar el APK en el móvil Android

1. Copia el fichero `app-debug.apk` al móvil (por USB, email, WhatsApp, etc.)
2. En el móvil, activa **"Instalar apps de fuentes desconocidas"**:
   - Ajustes → Seguridad → Instalar apps desconocidas → (activa para el navegador/gestor de archivos que uses)
3. Abre el fichero `.apk` desde el móvil y pulsa "Instalar"

---

## Primera configuración: Crear cuenta Gmail y Contraseña de aplicación

La app usa Gmail como servidor de envío. Necesitas **una cuenta Gmail dedicada** y una **Contraseña de aplicación** (distinta a la contraseña normal).

### Paso 1 — Crear la cuenta Gmail (5 minutos)

1. Ve a https://accounts.google.com/signup en tu móvil o PC
2. Crea una cuenta con un nombre identificativo, por ejemplo:
   - `dajasbaja.familiamart@gmail.com`
   - `pedidos.id.familiamart@gmail.com`
3. Completa el registro (necesitarás un número de teléfono para verificación)
4. **Guarda bien el email y la contraseña** de esta cuenta nueva

### Paso 2 — Activar la verificación en dos pasos (obligatorio para App Passwords)

1. Inicia sesión en https://myaccount.google.com con la cuenta recién creada
2. Ve a **Seguridad** (menú de la izquierda)
3. En "¿Cómo inicias sesión en Google?", pulsa **Verificación en dos pasos**
4. Sigue los pasos y actívala (puedes usar SMS)

### Paso 3 — Generar la Contraseña de aplicación

1. En https://myaccount.google.com → **Seguridad**
2. Busca **Contraseñas de aplicación** (aparece solo si tienes activada la verificación en 2 pasos)
3. Pulsa "Contraseñas de aplicación"
4. En el desplegable "Seleccionar aplicación" → elige **Correo**
5. En "Seleccionar dispositivo" → elige **Otro (nombre personalizado)** → escribe `DarDeBaja`
6. Pulsa **Generar**
7. Aparecerá una contraseña de **16 caracteres** (ejemplo: `abcd efgh ijkl mnop`)
   - **Cópiala ahora** — solo se muestra una vez
   - Los espacios no importan, puedes copiarla tal cual

### Paso 4 — Configurar la app en el móvil

Al abrir la app por primera vez, aparecerá automáticamente la pantalla de configuración:

- **Email remitente:** el Gmail que acabas de crear (ej: `dajasbaja.familiamart@gmail.com`)
- **Contraseña:** la **Contraseña de aplicación** de 16 caracteres (NO la contraseña normal de Gmail)

> El servidor SMTP ya está configurado: smtp.gmail.com · Puerto 587 · TLS

Pulsa "Guardar configuración" y ya está lista para usar.

---

## Cómo usar la app

1. Selecciona **Cocina** o **Recepciones**
2. Pulsa **"Seleccionar foto"** en cada producto (cámara o galería)
3. Introduce los **Kg** de cada producto
4. Pulsa **"+ Añadir otro producto"** si hay más referencias
5. Pulsa **"Enviar pedido"** — el correo se envía automáticamente

### El correo generado tiene este formato:
- **Asunto:** `Dar de baja - I+D`
- **Destinatario (pruebas):** `procesos@platostradicionales.com`
- **Fotos adjuntas** renombradas como: `Producto 1.jpg`, `Producto 2.jpg`, etc.
- **Saludo automático:** "Buenos días" (antes de 14:00) / "Buenas tardes" (después de 14:00)

---

## Notas técnicas

- Las credenciales se guardan **cifradas** en el dispositivo (AES-256)
- La app requiere conexión a internet para enviar
- Compatible con Android 7.0 en adelante
- Si el envío falla, la app mostrará el error real (credenciales incorrectas, sin conexión, etc.)
- **Importante:** usa siempre la Contraseña de aplicación de Gmail, nunca la contraseña normal
