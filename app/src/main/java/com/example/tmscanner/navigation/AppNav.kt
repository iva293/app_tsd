package com.example.tmscanner.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tmscanner.ui.menu.MainMenuScreen
import com.example.tmscanner.ui.scan.ScanScreen
import com.example.tmscanner.ui.scan.CameraScanScreen
import com.example.tmscanner.ui.send.SendScreen
import com.example.tmscanner.viewmodel.MainViewModel
import com.example.tmscanner.ui.settings.SettingsMenuScreen
import com.example.tmscanner.ui.settings.MailSettingsScreen
import com.example.tmscanner.ui.settings.ExchangeSettingsScreen


@Composable
fun AppNav(
    navController: NavHostController,
    vm: MainViewModel
) {

    NavHost(
        navController = navController,
        startDestination = "menu"
    ) {

        composable("menu") {
            MainMenuScreen(navController)
        }

        composable("scan") {
            ScanScreen(navController, vm)
        }

        composable("scan_camera") {
            CameraScanScreen(navController, vm)
        }

        composable("send") {
            SendScreen(
                vm = vm,
                nav = navController
            )
        }

        composable("settings") {
            SettingsMenuScreen(navController)
        }

        composable("smtp_settings") {
            MailSettingsScreen (
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable("exchange_settings") {
            ExchangeSettingsScreen(
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}