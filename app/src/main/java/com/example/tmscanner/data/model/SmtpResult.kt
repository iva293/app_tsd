package com.example.tmscanner.data.model

sealed class SmtpResult {

    data object Loading : SmtpResult()

    data class Success(
        val message: String = "SMTP OK"
    ) : SmtpResult()

    data class Error(
        val message: String
    ) : SmtpResult()
}