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

## Primera configuración en el móvil

Al abrir la app por primera vez, aparecerá automáticamente la pantalla de configuración:

- **Email remitente:** el correo desde el que se enviarán los pedidos (ej: pedidos@familia-martinez.es)
- **Contraseña:** la contraseña de esa cuenta de Outlook/Office 365

> El servidor SMTP ya está configurado: smtp.office365.com · Puerto 587 · TLS

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
