package com.example.tmscanner.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

class SettingsRepository(context: Context) {

    private val ds = context.dataStore

    // =========================
    // SAVE
    // =========================
    suspend fun save(settings: MailSettings) {
        ds.edit { prefs ->

            prefs[SettingsKeys.DISPLAY_NAME] = settings.displayName
            prefs[SettingsKeys.EMAIL] = settings.email
            prefs[SettingsKeys.PASSWORD] = settings.password
            prefs[SettingsKeys.TO_EMAIL] = settings.toEmail

            // SMTP
            prefs[SettingsKeys.SMTP_HOST] = settings.smtpHost
            prefs[SettingsKeys.SMTP_PORT] = settings.smtpPort.toString()
            prefs[SettingsKeys.SMTP_SECURITY] = settings.smtpSecurity.name

            prefs[SettingsKeys.SMTP_USE_SEPARATE] = settings.useSeparateSmtpAccount
            prefs[SettingsKeys.SMTP_EMAIL] = settings.smtpEmail
            prefs[SettingsKeys.SMTP_PASSWORD] = settings.smtpPassword

        }
    }

    // =========================
    // LOAD
    // =========================
    suspend fun load(): MailSettings {
        val prefs = ds.data.first()

        return MailSettings(
            displayName = prefs[SettingsKeys.DISPLAY_NAME] ?: "",
            email = prefs[SettingsKeys.EMAIL] ?: "",
            password = prefs[SettingsKeys.PASSWORD] ?: "",
            toEmail = prefs[SettingsKeys.TO_EMAIL] ?: "",

            // SMTP
            smtpHost = prefs[SettingsKeys.SMTP_HOST] ?: "",
            smtpPort = (prefs[SettingsKeys.SMTP_PORT] ?: "587").toIntOrNull() ?: 587,
            smtpSecurity = runCatching {
                SecurityMode.valueOf(
                    prefs[SettingsKeys.SMTP_SECURITY] ?: "STARTTLS"
                )
            }.getOrDefault(SecurityMode.STARTTLS),

            useSeparateSmtpAccount =
                prefs[SettingsKeys.SMTP_USE_SEPARATE] ?: false,

            smtpEmail = prefs[SettingsKeys.SMTP_EMAIL] ?: "",
            smtpPassword = prefs[SettingsKeys.SMTP_PASSWORD] ?: "",

        )
    }
}