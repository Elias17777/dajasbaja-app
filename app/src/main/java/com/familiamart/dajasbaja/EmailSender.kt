package com.familiamart.dajasbaja

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.activation.DataHandler
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

object EmailSender {

    /**
     * Envía el email de pedido con fotos adjuntas.
     * Se debe llamar desde una coroutine (lifecycleScope.launch).
     */
    suspend fun sendEmail(
        context:    Context,
        smtpHost:   String,
        smtpPort:   Int,
        fromEmail:  String,
        password:   String,
        toEmails:   List<String>,
        department: String,
        kgValues:   List<String>,
        photoUris:  List<Uri>,
        camaraInfo: String = ""
    ) = withContext(Dispatchers.IO) {

        // --- Configuración SMTP para Ethereal (STARTTLS en puerto 587, servidor de pruebas) ---
        val props = Properties().apply {
            put("mail.smtp.auth",                "true")
            put("mail.smtp.starttls.enable",     "true")
            put("mail.smtp.starttls.required",   "true")
            put("mail.smtp.host",                smtpHost)
            put("mail.smtp.port",                smtpPort.toString())
            put("mail.smtp.ssl.trust",           smtpHost)
            put("mail.smtp.connectiontimeout",   "15000")
            put("mail.smtp.timeout",             "15000")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() =
                PasswordAuthentication(fromEmail, password)
        })

        // --- Construir el mensaje ---
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(fromEmail))

        val recipients = toEmails
            .map { InternetAddress(it) }
            .toTypedArray()
        message.setRecipients(Message.RecipientType.TO, recipients)

        message.subject = "Dar de baja - I+D"
        message.sentDate = Date()

        // --- Multipart: cuerpo + adjuntos ---
        val multipart = MimeMultipart()

        // Cuerpo del correo (texto plano, UTF-8)
        val bodyPart = MimeBodyPart()
        bodyPart.setText(buildEmailBody(department, kgValues, camaraInfo), "utf-8", "plain")
        multipart.addBodyPart(bodyPart)

        // Adjuntar fotos renombradas como "Producto 1.jpg", "Producto 2.jpg", ...
        photoUris.forEachIndexed { i, uri ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("No se pudo leer la foto del Producto ${i + 1}")

                val bytes = inputStream.readBytes()
                inputStream.close()

                val attachPart = MimeBodyPart()
                attachPart.dataHandler = DataHandler(
                    ByteArrayDataSource(bytes, "image/jpeg")
                )
                attachPart.fileName = "Producto ${i + 1}.jpg"
                multipart.addBodyPart(attachPart)
            } catch (e: Exception) {
                throw Exception("Error adjuntando Producto ${i + 1}: ${e.message}")
            }
        }

        message.setContent(multipart)

        // --- Enviar ---
        Transport.send(message)
    }

    // -------------------------------------------------------------------------
    // Construye el cuerpo del correo según la especificación
    // -------------------------------------------------------------------------
    private fun buildEmailBody(department: String, kgValues: List<String>, camaraInfo: String): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val saludo = if (hour < 14) "Buenos días" else "Buenas tardes"

        return buildString {
            appendLine("$saludo,")
            appendLine()
            appendLine("Hemos cogido del almacén de $department las siguientes referencias:")
            appendLine()
            kgValues.forEachIndexed { i, kg ->
                appendLine("  - Producto ${i + 1} = $kg Kg")
            }
            if (camaraInfo.isNotEmpty()) {
                appendLine()
                appendLine("Cámara: $camaraInfo")
            }
            appendLine()
            appendLine("Cualquier cosa lo vemos.")
            appendLine()
            append("Un saludo,")
        }
    }
}
