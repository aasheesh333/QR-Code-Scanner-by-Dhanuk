package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dhanuk.quickscanpro.config.AppConfig
import com.dhanuk.quickscanpro.ui.composables.BannerAd
import com.dhanuk.quickscanpro.ui.composables.LiquidBackground
import com.dhanuk.quickscanpro.ui.navigation.BottomNavItem
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = viewModel()
    val onboardingCompleted by settingsViewModel.onboardingCompleted.collectAsState()

    when {
        onboardingCompleted == null -> {
            Box(modifier = Modifier.fillMaxSize())
        }
        onboardingCompleted == false -> {
            OnboardingScreen(
                onFinished = {
                    settingsViewModel.completeOnboarding()
                }
            )
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize()) {
                LiquidBackground()
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        Column {
                            BannerAd(adUnitId = AppConfig.AdMob.BANNER_AD_UNIT_ID_HOME)
                            FloatingBottomNav(navController)
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Navigation(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                onScan = { result ->
                    val encodedUrl = URLEncoder.encode(result, StandardCharsets.UTF_8.toString())
                    navController.navigate("result/$encodedUrl")
                },
                onBatchScan = {
                    navController.navigate("batch_scan")
                },
                onCompareScan = {
                    navController.navigate("compare_scan")
                },
                onViewAllHistory = {
                    navController.navigate(BottomNavItem.History.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(BottomNavItem.Generate.route) {
            QRGeneratorScreen()
        }
        composable(BottomNavItem.History.route) {
            HistoryScreen(
                onOpenVault = { navController.navigate("vault") },
                onOpenCompare = { navController.navigate("compare_scan") }
            )
        }
        composable(BottomNavItem.Analytics.route) {
            AnalyticsScreen()
        }
        composable(BottomNavItem.Settings.route) {
            SettingsScreen(
                onNavigateToAbout = { navController.navigate("about") },
                onNavigateToThemeStudio = { navController.navigate("theme_studio") }
            )
        }
        composable(
            route = "result/{data}",
            arguments = listOf(navArgument("data") { type = NavType.StringType })
        ) { backStackEntry ->
            val data = backStackEntry.arguments?.getString("data") ?: ""
            ResultScreen(
                data = data,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProduct = { barcode ->
                    val encoded = URLEncoder.encode(barcode, StandardCharsets.UTF_8.toString())
                    navController.navigate("product/$encoded")
                }
            )
        }
        composable("batch_scan") {
            BatchScanScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("compare_scan") {
            CompareScanScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("vault") {
            VaultScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("theme_studio") {
            ThemeStudioScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = "product/{barcode}",
            arguments = listOf(navArgument("barcode") { type = NavType.StringType })
        ) { backStackEntry ->
            val barcode = backStackEntry.arguments?.getString("barcode") ?: ""
            ProductLookupScreen(
                barcode = barcode,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("about") {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("about_us") { AboutUsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("contact_us") { ContactUsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("privacy_policy") { PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("permissions") { PermissionsUsageScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("terms") { TermsAndConditionsScreen(onNavigateBack = { navController.popBackStack() }) }
    }
}

/**
 * Floating frosted-glass pill navigation bar — the signature
 * Lumina Glass bottom bar. Active tab is a glowing purple circle.
 */
@Composable
fun FloatingBottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = RoundedCornerShape(0.dp)
        )
    ) {
        BottomNavItem.entries.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
