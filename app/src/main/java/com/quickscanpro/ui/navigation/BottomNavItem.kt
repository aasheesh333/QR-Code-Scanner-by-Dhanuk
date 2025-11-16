package com.quickscanpro.ui.navigation

import com.quickscanpro.R

sealed class BottomNavItem(val route: String, val icon: Int, val title: String) {
    object Home : BottomNavItem("home", R.drawable.ic_home, "Home")
    object History : BottomNavItem("history", R.drawable.ic_history, "History")
    object About : BottomNavItem("about", R.drawable.ic_about, "About")
}
