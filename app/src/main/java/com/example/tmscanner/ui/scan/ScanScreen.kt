package com.example.tmscanner.ui.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tmscanner.viewmodel.MainViewModel
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

// ====================
// SCAN SCREEN
// ====================
@Composable
fun ScanScreen(
    nav: NavController,
    vm: MainViewModel
) {
    // ====================
    // LOCAL INPUT STATE
    // ====================
    var input by remember { mutableStateOf("") }


    // ====================
    // ROOT LAYOUT
    // ====================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        // BACK BUTTON
        OutlinedButton(onClick = { nav.popBackStack() }) {
            Text("Назад")
        }

        Spacer(Modifier.height(12.dp))

        // SCREEN TITLE
        Text("Сканирование", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        // QR INPUT FIELD
        TextField(
            value = input,
            onValueChange = { input = it },
            singleLine = true,
            placeholder = {
                Text("Сканируйте QR или используйте камеру...")
            },

            // the Done button on the keyboard
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),

            // processing Done
            keyboardActions = KeyboardActions(
                onDone = {
                    vm.handleScan(input)
                    input = ""
                }
            ),

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // ACTION BUTTONS
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ADD BUTTON
            Button(
                onClick = {
                    if (input.isBlank()) return@Button
                    vm.handleScan(input.trim())
                    input = ""
                }
            ) {
                Text("Добавить")
            }

            // CLEAR BUTTON
            OutlinedButton(
                onClick = { vm.clearList() }
            ) {
                Text("Очистить")
            }
        }

        Spacer(Modifier.height(12.dp))

        // =====================
        // OPEN CAMERA SCREEN
        // =====================
        Button(
            onClick = {
                nav.navigate("scan_camera")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сканировать камерой")
        }

        Spacer(Modifier.height(12.dp))

        Text("Список QR:")

        Spacer(Modifier.height(8.dp))

        // QR LIST
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(vm.list, key = { it }) { qr ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = qr,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}