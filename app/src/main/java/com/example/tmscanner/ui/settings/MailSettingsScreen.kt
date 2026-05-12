package com.example.tmscanner.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.tmscanner.data.SecurityMode
import com.example.tmscanner.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun MailSettingsScreen(
    vm: MainViewModel,
    onBack: () -> Unit
) {

    // SETTINGS STATE
    val settings = vm.mailSettings.value

    // SNACKBAR STATE
    val snackbarHostState = remember { SnackbarHostState() }

    // COMPOSE SCOPE
    val scope = rememberCoroutineScope()

    // PASSWORD VISIBILITY
    var passwordVisible by remember { mutableStateOf(false) }

    // SMTP PORT TEXT STATE
    var smtpPortText by remember { mutableStateOf("") }

    // SYNC PORT FROM SETTINGS
    LaunchedEffect(settings.smtpPort) {
        smtpPortText = settings.smtpPort.toString()
    }

    // ROOT SCAFFOLD
    Scaffold(
        // snackbar container
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        // ROOT CONTENT
        Column(
            modifier = Modifier
                .padding(padding) // scaffold padding
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // scroll support
                .padding(16.dp) // inner padding
        ) {

            // BACK BUTTON
            OutlinedButton(onClick = onBack) {
                Text("Назад")
            }

            Spacer(Modifier.height(16.dp))

            // SCREEN TITLE
            Text(
                "Почтовые настройки",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(16.dp))

            // =====================
            // USER SETTINGS
            // =====================

            // DISPLAY NAME
            OutlinedTextField(
                value = settings.displayName,
                onValueChange = { value ->
                    vm.updateMailSettings { state ->
                        state.copy(displayName = value)
                    }
                },
                label = { Text("Имя отправителя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // EMAIL
            OutlinedTextField(
                value = settings.email,
                onValueChange = { value ->
                    vm.updateMailSettings { state ->
                        state.copy(email = value)
                    }
                },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // PASSWORD
            OutlinedTextField(
                value = settings.password,
                onValueChange = { value ->
                    vm.updateMailSettings { state ->
                        state.copy(password = value)
                    }
                },
                label = { Text("Пароль") },
                singleLine = true,
                // hiding/showing password
                visualTransformation =
                    if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                // icon eye
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector =
                                if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // =====================
            // SMTP CARD
            // =====================

            Card(Modifier.fillMaxWidth()) {

                Column(Modifier.padding(16.dp)) {

                    Text("SMTP (отправка)")

                    // SMTP HOST
                    OutlinedTextField(
                        value = settings.smtpHost,
                        onValueChange = { value ->
                            vm.updateMailSettings { state ->
                                state.copy(smtpHost = value)
                            }
                        },
                        label = { Text("SMTP Host") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // SEPARATE SMTP ACCOUNT SWITCH
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {

                        Switch(
                            checked = settings.useSeparateSmtpAccount,
                            onCheckedChange = { enabled ->
                                vm.updateMailSettings {
                                    it.copy(useSeparateSmtpAccount = enabled)
                                }
                            }
                        )

                        Spacer(Modifier.width(8.dp))

                        Text("Использовать отдельный SMTP аккаунт")
                    }

                    // SEPARATE SMTP ACCOUNT FIELDS
                    if (settings.useSeparateSmtpAccount) {

                        OutlinedTextField(
                            value = settings.smtpEmail,
                            onValueChange = { value ->
                                vm.updateMailSettings { it.copy(smtpEmail = value) }
                            },
                            label = { Text("SMTP Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // SMTP PASSWORD
                        OutlinedTextField(
                            value = settings.smtpPassword,
                            onValueChange = { value ->
                                vm.updateMailSettings { it.copy(smtpPassword = value) }
                            },
                            label = { Text("SMTP Password") },
                            singleLine = true,
                            visualTransformation =
                                if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector =
                                            if (passwordVisible) Icons.Default.Visibility
                                            else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // SMTP PORT
                    OutlinedTextField(
                        value = smtpPortText,
                        onValueChange = { value ->
                            smtpPortText = value
                            value.toIntOrNull()?.let { port ->
                                vm.updateMailSettings { state ->
                                    state.copy(smtpPort = port)
                                }
                            }
                        },
                        label = { Text("SMTP Port") },
                        placeholder = { Text("587 / 465 / 25") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // =====================
                    // SMTP SECURITY MODE
                    // =====================
                    Text("Security")

                    Row {
                        RadioButton(
                            selected = settings.smtpSecurity == SecurityMode.NONE,
                            onClick = {
                                vm.updateMailSettings {
                                    it.copy(smtpSecurity = SecurityMode.NONE)
                                }
                            }
                        )
                        Text("NONE")
                    }

                    Row {
                        RadioButton(
                            selected = settings.smtpSecurity == SecurityMode.STARTTLS,
                            onClick = {
                                vm.updateMailSettings {
                                    it.copy(smtpSecurity = SecurityMode.STARTTLS)
                                }
                            }
                        )
                        Text("STARTTLS")
                    }

                    Row {
                        RadioButton(
                            selected = settings.smtpSecurity == SecurityMode.SSL,
                            onClick = {
                                vm.updateMailSettings {
                                    it.copy(smtpSecurity = SecurityMode.SSL)
                                }
                            }
                        )
                        Text("SSL")
                    }

                    Spacer(Modifier.height(12.dp))

                    // ==================
                    // TEST SMTP BUTTON
                    // ==================
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            vm.testSmtp(vm.mailSettings.value) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(it)
                                }
                            }
                        }
                    ) {
                        Text("🔌 Проверить SMTP")
                    }
                }
            }
        }
    }
}