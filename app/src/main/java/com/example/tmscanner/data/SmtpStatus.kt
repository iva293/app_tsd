package com.example.tmscanner.data

sealed class SmtpStatus {

    data object Idle : SmtpStatus()
    data object Connecting : SmtpStatus()
    data object Success : SmtpStatus()

    data class Error(val message: String) : SmtpStatus()
}