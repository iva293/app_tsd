package com.example.tmscanner.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingsMenuScreen(nav: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ){

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            OutlinedButton(onClick = { nav.popBackStack() }) {
                Text("Назад")
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Настройки",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(32.dp))

        // =========================
        // SMTP
        // =========================

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { nav.navigate("smtp_settings") }
        ) {
            Text("📤 Настройка почты")
        }

        Spacer(Modifier.height(12.dp))

        // =========================
        // EXCHANGE
        // =========================

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { nav.navigate("exchange_settings") }
        ) {
            Text("📩 Обмен")
        }
    }
}