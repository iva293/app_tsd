package com.example.tmscanner.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tmscanner.viewmodel.MainViewModel


@Composable
fun ExchangeSettingsScreen(
    vm: MainViewModel,
    onBack: () -> Unit
) {

    // email sync with VM
    var to by remember { mutableStateOf(vm.mailSettings.value.toEmail) }

    // status take from VM
    val status by vm.sendStatus

    // sync when settings edit
    LaunchedEffect(vm.mailSettings.value) {
        to = vm.mailSettings.value.toEmail
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        OutlinedButton(onClick = onBack) {
            Text("Назад")
        }

        Spacer(Modifier.height(16.dp))

        Text("Обмен", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        // =========================
        // EMAIL
        // =========================
        OutlinedTextField(
            value = to,
            onValueChange = {
                to = it
                vm.updateMailSettings { current ->
                    current.copy(toEmail = it)
                }
            },
            label = { Text("Email получателя") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // =========================
        // TEST EMAIL
        // =========================
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                vm.sendTestEmail(vm.mailSettings.value)
            }
        ) {
            Text("📧 Тестовое письмо")
        }

        Spacer(Modifier.height(16.dp))

        // =========================
        // STATUS
        // =========================
        if (status.isNotEmpty()) {
            Text(text = status)
        }
    }
}