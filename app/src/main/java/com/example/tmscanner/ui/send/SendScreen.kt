package com.example.tmscanner.ui.send

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tmscanner.viewmodel.MainViewModel
import kotlinx.coroutines.launch

// ===============
// SEND SCREEN
// ===============
@Composable
fun SendScreen(
    vm: MainViewModel,
    nav: NavController
) {

    val snackbarHostState = remember { SnackbarHostState() } // SNACKBAR STATE
    val scope = rememberCoroutineScope() // COMPOSE COROUTINE SCOPE
    val isSending by vm.isSending

    // ==============
    // ROOT SCAFFOLD
    // ==============
    Scaffold(

        // SNACKBAR HOST
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }
    ) { padding ->

        // ===============
        // MAIN CONTENT
        // ===============
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // BACK BUTTON
            OutlinedButton(onClick = { nav.popBackStack() }) {
                Text("Назад")
            }

            Spacer(Modifier.height(32.dp))

            // SCREEN TITLE
            Text(
                text = "Отправка файла",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(24.dp))

            // SEND BUTTON
            Button(
                // SEND ACTION
                onClick = {
                    vm.sendQrList { result ->
                        scope.launch {
                            snackbarHostState.showSnackbar(result)
                        }
                    }
                },
                enabled = !isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {

                // LOADING STATE
                if (vm.isSending.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Отправка...")
                } else {
                    Text("Отправить файл")
                }
            }
        }
    }
}