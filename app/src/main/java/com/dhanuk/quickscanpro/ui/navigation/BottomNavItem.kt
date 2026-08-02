package com.dhanuk.quickscanpro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
) {
    Home("home", Icons.Filled.Home, "Home"),
    Generate("generate", Icons.Filled.AddBox, "Generate"),
    History("history", Icons.Filled.History, "History"),
    Settings("settings", Icons.Filled.Settings, "Settings")
}
