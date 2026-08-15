package com.dhanuk.quickscanpro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
) {
    Home("home", Icons.Filled.QrCodeScanner, "Scan"),
    Generate("generate", Icons.Filled.AddBox, "Create"),
    History("history", Icons.Filled.History, "History"),
    Settings("settings", Icons.Filled.Tune, "Settings")
}
