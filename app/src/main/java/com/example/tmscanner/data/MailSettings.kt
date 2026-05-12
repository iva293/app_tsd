package com.example.tmscanner.data

data class MailSettings(
    val displayName: String = "",

    val email: String = "",
    val password: String = "",

    // =====================
    // SMTP
    // =====================
    val smtpHost: String = "",
    val smtpPort: Int = 587,
    val smtpSecurity: SecurityMode = SecurityMode.STARTTLS,

    // =====================
    // SMTP user
    // =====================
    val useSeparateSmtpAccount: Boolean = false,
    val smtpEmail: String = "",
    val smtpPassword: String = "",

    // =====================
    // OTHER
    // =====================
    val toEmail: String = ""
)