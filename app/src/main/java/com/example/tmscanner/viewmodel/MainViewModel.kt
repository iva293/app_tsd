package com.example.tmscanner.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmscanner.data.*
import com.example.tmscanner.R
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SettingsRepository(application)

    private val appName: String =
        getApplication<Application>().getString(R.string.app_name)

    // =========================
    // UI STATE
    // =========================

    val list = mutableStateListOf<String>()
    val logs = mutableStateListOf<String>()

    val isSending = mutableStateOf(false)
    val sendStatus = mutableStateOf("")

    val smtpStatus = mutableStateOf<SmtpStatus>(SmtpStatus.Idle)

    var mailSettings = mutableStateOf(MailSettings())

    // =========================
    // SCAN STATE (FIXED)
    // =========================

    private var lastScanValue: String? = null
    private var lastScanTime = 0L

    private val cooldownMs = 20L

    init {
        loadSettings()
    }

    // =========================
    // QR SCAN HANDLER
    // =========================

    fun handleScan(value: String) {

        val clean = value.trim()

        if (clean.isEmpty()) return

        val now = System.currentTimeMillis()

        // 🔥 1. защита от повторного срабатывания камеры
        if (clean == lastScanValue && now - lastScanTime < cooldownMs) return

        lastScanValue = clean
        lastScanTime = now

        // 🔥 2. защита от дублей в списке (Set-логика)
        if (list.contains(clean)) return

        // новые сверху
        list.add(0, clean)

        addLog("📦 Добавлено: $clean")
    }

    fun clearList() {
        list.clear()
        addLog("🧹 Список очищен")
    }

    // =========================
    // SMTP TEST
    // =========================

    fun testSmtp(settings: MailSettings, callback: (String) -> Unit) {
        viewModelScope.launch {

            smtpStatus.value = SmtpStatus.Connecting
            callback("🔌 Подключение SMTP...")

            val result = EmailClient.testConnection(settings)

            result
                .onSuccess {
                    smtpStatus.value = SmtpStatus.Success
                    callback("✅ SMTP OK")
                }
                .onFailure { e ->
                    smtpStatus.value = SmtpStatus.Error(e.message ?: "Ошибка")
                    callback("❌ ${e.message}")
                }
        }
    }

    // =========================
    // SEND TEST EMAIL
    // =========================

    fun sendTestEmail(settings: MailSettings) {
        viewModelScope.launch {
            try {
                sendStatus.value = "📤 Отправка..."

                EmailClient.sendMail(
                    settings = settings,
                    subject = "SMTP TEST",
                    body = """
                        📊 DIAGNOSTIC REPORT — $appName

                        Status: SUCCESS
                        Connection: SMTP OK
                        Auth: OK

                        ✔ Mail pipeline is working correctly
                    """.trimIndent()
                )

                sendStatus.value = "✅ Письмо отправлено"

            } catch (e: Exception) {
                sendStatus.value = "❌ ${e.message}"
            }
        }
    }

    // =========================
    // SEND CSV
    // =========================

    fun sendQrList(onResult: (String) -> Unit) {

        // PROTECTION AGAINST DOUBLE SEND
        if (isSending.value) return
        viewModelScope.launch {

            // EMPTY LIST CHECK
            if (list.isEmpty()) {
                sendStatus.value = "❌ Список пуст"
                onResult("❌ Список пуст")
                return@launch
            }

            // START LOADING
            isSending.value = true
            sendStatus.value = "📤 Отправка..."
            onResult("📤 Отправка...")

            try {

                // LOAD EMAIL SETTINGS
                val settings = mailSettings.value

                // CREATE CSV FILE
                val file = File(getApplication<Application>().cacheDir, "inv.csv")

                // DELETE OLD FILE
                if (file.exists()) {
                    file.delete()
                }

                // GENERATION CSV
                CsvUtil.create(list, file)

                // SEND EMAIL
                EmailClient.sendFile(
                    settings = settings,
                    file = file,
                    subject = "tsd_inv"
                )

                // SUCCESS
                sendStatus.value = "✅ Отправлено"
                onResult("✅ Отправлено")

            } catch (e: Exception) {

                // ERROR HANDLING
                val error = e.message ?: "Ошибка"

                sendStatus.value = "❌ $error"
                onResult("❌ $error")

            } finally {

                // STOP LOADING
                isSending.value = false
            }
        }
    }

    // =========================
    // LOGS
    // =========================

    fun addLog(text: String) {
        logs.add(text)
    }

    // =========================
    // SETTINGS
    // =========================

    private fun loadSettings() {
        viewModelScope.launch {
            runCatching { repo.load() }
                .onSuccess { mailSettings.value = it }
        }
    }

    fun updateMailSettings(update: (MailSettings) -> MailSettings) {
        viewModelScope.launch {
            val newSettings = update(mailSettings.value)
            mailSettings.value = newSettings
            repo.save(newSettings)
        }
    }
}