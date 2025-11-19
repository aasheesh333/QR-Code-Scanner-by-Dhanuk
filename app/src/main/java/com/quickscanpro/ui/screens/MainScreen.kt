package com.quickscanpro.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quickscanpro.ui.navigation.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Navigation(navController = navController)
        }
    }
}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(onScan = { result ->
                val encodedUrl = URLEncoder.encode(result, StandardCharsets.UTF_8.toString())
                navController.navigate("result/$encodedUrl")
            })
        }
        composable(BottomNavItem.History.route) {
            HistoryScreen()
        }
        composable(BottomNavItem.About.route) {
            AboutScreen(
                onNavigateToAboutUs = { navController.navigate("about_us") },
                onNavigateToContactUs = { navController.navigate("contact_us") },
                onNavigateToPrivacyPolicy = { navController.navigate("privacy_policy") },
                onNavigateToPermissions = { navController.navigate("permissions") },
                onNavigateToTerms = { navController.navigate("terms") }
            )
        }
        composable("result/{data}") { backStackEntry ->
            val data = backStackEntry.arguments?.getString("data") ?: ""
            ResultScreen(data = data, onNavigateBack = { navController.popBackStack() })
        }
        composable("about_us") {
            AboutUsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("contact_us") {
            ContactUsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("privacy_policy") {
            PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("permissions") {
            PermissionsUsageScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("terms") {
            TermsAndConditionsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.About
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
