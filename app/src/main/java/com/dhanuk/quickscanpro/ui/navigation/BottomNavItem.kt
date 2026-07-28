package com.dhanuk.quickscanpro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
) {
    Home("home", Icons.Filled.Home, "Scan"),
    Generate("generate", Icons.Filled.QrCode2, "Create"),
    History("history", Icons.Filled.History, "History"),
    Analytics("analytics", Icons.Filled.Analytics, "Stats"),
    Settings("settings", Icons.Filled.Settings, "Settings")
}
