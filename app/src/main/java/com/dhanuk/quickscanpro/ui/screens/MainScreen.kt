package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.dhanuk.quickscanpro.ui.navigation.BottomNavItem
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
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
            Scaffold(
                bottomBar = {
                    Column {
                        BannerAd(adUnitId = AppConfig.AdMob.BANNER_AD_UNIT_ID_HOME)
                        BottomNavigationBar(navController)
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
                }
            )
        }
        composable(BottomNavItem.Generate.route) {
            QRGeneratorScreen()
        }
        composable(BottomNavItem.History.route) {
            HistoryScreen()
        }
        composable(BottomNavItem.Analytics.route) {
            AnalyticsScreen()
        }
        composable(BottomNavItem.Settings.route) {
            SettingsScreen(
                onNavigateToAbout = { navController.navigate("about") }
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
            AboutScreen(
                onNavigateToAboutUs = { navController.navigate("about_us") },
                onNavigateToContactUs = { navController.navigate("contact_us") },
                onNavigateToPrivacyPolicy = { navController.navigate("privacy_policy") },
                onNavigateToPermissions = { navController.navigate("permissions") },
                onNavigateToTerms = { navController.navigate("terms") }
            )
        }
        composable("about_us") { AboutUsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("contact_us") { ContactUsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("privacy_policy") { PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("permissions") { PermissionsUsageScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("terms") { TermsAndConditionsScreen(onNavigateBack = { navController.popBackStack() }) }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
