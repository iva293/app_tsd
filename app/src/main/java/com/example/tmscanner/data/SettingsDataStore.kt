package com.example.tmscanner.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "settings")

object SettingsKeys {

    val DISPLAY_NAME = stringPreferencesKey("display_name")
    val EMAIL = stringPreferencesKey("email")
    val PASSWORD = stringPreferencesKey("password")
    val TO_EMAIL = stringPreferencesKey("to_email")

    // =====================
    // SMTP
    // =====================
    val SMTP_HOST = stringPreferencesKey("smtp_host")
    val SMTP_PORT = stringPreferencesKey("smtp_port")
    val SMTP_SECURITY = stringPreferencesKey("smtp_security")

    val SMTP_USE_SEPARATE = booleanPreferencesKey("smtp_use_separate")
    val SMTP_EMAIL = stringPreferencesKey("smtp_email")
    val SMTP_PASSWORD = stringPreferencesKey("smtp_password")

}