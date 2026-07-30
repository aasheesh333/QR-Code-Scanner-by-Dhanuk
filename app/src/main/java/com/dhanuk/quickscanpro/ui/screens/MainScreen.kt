package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dhanuk.quickscanpro.config.AppConfig
import com.dhanuk.quickscanpro.ui.composables.BannerAd
import com.dhanuk.quickscanpro.ui.navigation.BottomNavItem
import com.dhanuk.quickscanpro.viewmodel.QRGeneratorViewModel
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        onboardingCompleted == false -> {
            OnboardingScreen {
                settingsViewModel.completeOnboarding()
            }
        }
        else -> {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    Column {
                        BannerAd(adUnitId = AppConfig.AdMob.BANNER_AD_UNIT_ID_HOME)
                        AppBottomBar(navController)
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AppNavigation(navController)
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBarRoutes = BottomNavItem.entries.map { it.route } +
        listOf("result/{data}")

    if (currentRoute == null) return
    if (!showBarRoutes.contains(currentRoute.split("/").first().substringBefore("?"))) {
        return
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
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
                icon = { Icon(item.icon, contentDescription = item.title) },
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

@Composable
private fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                onScan = { result ->
                    val encoded = URLEncoder.encode(result, StandardCharsets.UTF_8.toString())
                    navController.navigate("result/$encoded")
                },
                onViewAllHistory = {
                    navController.navigate(BottomNavItem.History.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onOpenBatch = { navController.navigate("batch_scan") },
                onOpenCompare = { navController.navigate("compare_scan") },
                onOpenVault = { navController.navigate("vault") },
                onOpenTimeline = { navController.navigate("timeline") },
                onOpenTemplates = { navController.navigate("templates") },
                onOpenLeakCheck = { navController.navigate("leak_check") },
                onOpenSettings = {
                    navController.navigate(BottomNavItem.Settings.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onOpenGenerate = {
                    navController.navigate(BottomNavItem.Generate.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(BottomNavItem.Generate.route) { QRGeneratorScreen() }
        composable(BottomNavItem.History.route) {
            HistoryScreen(
                onOpenVault = { navController.navigate("vault") },
                onOpenCompare = { navController.navigate("compare_scan") }
            )
        }
        composable(BottomNavItem.Analytics.route) { AnalyticsScreen() }
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
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("batch_scan") { BatchScanScreen { navController.popBackStack() } }
        composable("compare_scan") { CompareScanScreen { navController.popBackStack() } }
        composable("vault") {
            VaultScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("theme_studio") { ThemeStudioScreen { navController.popBackStack() } }
        composable("timeline") { TimelineScreen { navController.popBackStack() } }
        composable("templates") {
            val qrVm: QRGeneratorViewModel = viewModel()
            TemplatesScreen(
                onNavigateBack = { navController.popBackStack() },
                onUseTemplate = { template ->
                    qrVm.prefill(
                        type = template.type,
                        p1 = template.prefill.f1,
                        p2 = template.prefill.f2,
                        p3 = template.prefill.f3,
                        p4 = template.prefill.f4
                    )
                    navController.navigate(BottomNavItem.Generate.route)
                }
            )
        }
        composable("leak_check") { LeakCheckScreen { navController.popBackStack() } }
        composable(
            route = "product_lookup/{barcode}",
            arguments = listOf(navArgument("barcode") { type = NavType.StringType })
        ) { backStackEntry ->
            ProductLookupScreen(
                barcode = backStackEntry.arguments?.getString("barcode") ?: "",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("about") { AboutScreen { navController.popBackStack() } }
        composable("about_us") { AboutUsScreen { navController.popBackStack() } }
        composable("contact_us") { ContactUsScreen { navController.popBackStack() } }
        composable("privacy_policy") { PrivacyPolicyScreen { navController.popBackStack() } }
        composable("permissions") { PermissionsUsageScreen { navController.popBackStack() } }
        composable("terms") { TermsAndConditionsScreen { navController.popBackStack() } }
    }
}
