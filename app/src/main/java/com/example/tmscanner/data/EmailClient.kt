package com.example.tmscanner.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.internet.*


object EmailClient {

    // =========================
    // SMTP SESSION
    // =========================
    private fun smtpSession(settings: MailSettings): Session {

        val props = Properties().apply {

            put(MailProps.SMTP_AUTH, "true")
            put(MailProps.SMTP_HOST, settings.smtpHost)
            put(MailProps.SMTP_PORT, settings.smtpPort.toString())

            put(MailProps.SMTP_LOCAL, settings.smtpHost)

            when (settings.smtpSecurity) {
                SecurityMode.SSL -> {
                    put(MailProps.SMTP_SSL_ENABLE, "true")
                    put(MailProps.SMTP_STARTTLS, "false")
                }

                SecurityMode.STARTTLS -> {
                    put(MailProps.SMTP_STARTTLS, "true")
                    put(MailProps.SMTP_SSL_ENABLE, "false")
                }

                SecurityMode.NONE -> {
                    put(MailProps.SMTP_STARTTLS, "false")
                    put(MailProps.SMTP_SSL_ENABLE, "false")
                }
            }

            put(MailProps.SMTP_SSL_TRUST, "*")

            put(MailProps.SMTP_CONNECTION_TIMEOUT, "10000")
            put(MailProps.SMTP_TIMEOUT, "10000")
            put(MailProps.SMTP_WRITE_TIMEOUT, "10000")
        }

        return Session.getInstance(props).apply {
            debug = MailProps.MAIL_DEBUG
        }
    }

    // =========================
    // SMTP AUTH
    // =========================
    private fun smtpAuth(settings: MailSettings): Pair<String, String> {
        return if (settings.useSeparateSmtpAccount) {
            settings.smtpEmail to settings.smtpPassword
        } else {
            settings.email to settings.password
        }
    }

    // =========================
    // CONNECT
    // =========================
    private fun connect(settings: MailSettings, transport: Transport) {

        val (user, pass) = smtpAuth(settings)

        transport.connect(
            settings.smtpHost,
            settings.smtpPort,
            user,
            pass
        )
    }

    // =========================
    // TEST SMTP
    // =========================
    suspend fun testConnection(settings: MailSettings): Result<Unit> =
        withContext(Dispatchers.IO) {

            val session = smtpSession(settings)
            val transport = session.getTransport("smtp")

            return@withContext try {

                connect(settings, transport)
                Result.success(Unit)

            } catch (e: Exception) {

                Result.failure(Exception("SMTP error: ${e.message}"))

            } finally {
                try { transport.close() } catch (_: Exception) {}
            }
        }


    // =========================
    // SEND MAIL
    // =========================
    suspend fun sendMail(
        settings: MailSettings,
        subject: String,
        body: String
    ) = withContext(Dispatchers.IO) {

        val session = smtpSession(settings)

        //val (smtpUser, _) = smtpAuth(settings)

        val msg = MimeMessage(session).apply {

            setFrom(InternetAddress(settings.email, settings.displayName))

            setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(settings.toEmail)
            )

            this.subject = subject
            setText(body, "UTF-8")
        }

        val transport = session.getTransport("smtp")

        try {
            connect(settings, transport)
            transport.sendMessage(msg, msg.allRecipients)
        } finally {
            try { transport.close() } catch (_: Exception) {}
        }
    }

    // =========================
    // SEND FILE
    // =========================
    suspend fun sendFile(
        settings: MailSettings,
        file: File,
        subject: String,
        to: String = settings.toEmail
    ) = withContext(Dispatchers.IO) {

        val session = smtpSession(settings)

        val msg = MimeMessage(session).apply {

            setFrom(InternetAddress(settings.email, settings.displayName))

            setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(to)
            )

            this.subject = subject
        }

        val multipart = MimeMultipart().apply {

            addBodyPart(MimeBodyPart().apply {
                setText("Во вложении файл")
            })

            addBodyPart(MimeBodyPart().apply {
                val source = FileDataSource(file)
                dataHandler = DataHandler(source)
                fileName = file.name
            })
        }

        msg.setContent(multipart)

        val transport = session.getTransport("smtp")

        try {
            connect(settings, transport)
            transport.sendMessage(msg, msg.allRecipients)
        } finally {
            try { transport.close() } catch (_: Exception) {}
        }
    }
}