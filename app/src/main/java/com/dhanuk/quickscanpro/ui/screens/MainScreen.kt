package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.dhanuk.quickscanpro.ui.theme.GlassBorderDark
import com.dhanuk.quickscanpro.ui.theme.GlassBorderLight
import com.dhanuk.quickscanpro.ui.theme.GlassFillDark
import com.dhanuk.quickscanpro.ui.theme.GlassFillLight
import com.dhanuk.quickscanpro.ui.theme.LuminaPrimary
import com.dhanuk.quickscanpro.ui.theme.LuminaPrimaryGlow
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
    val dark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = LuminaPrimary.copy(alpha = 0.3f),
                    spotColor = LuminaPrimary.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(if (dark) GlassFillDark else GlassFillLight)
                .border(
                    1.dp,
                    if (dark) GlassBorderDark else GlassBorderLight,
                    RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEach { item ->
                val selected = currentRoute == item.route
                val itemColor = if (selected) Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .then(
                            if (selected) Modifier.background(
                                if (dark) LuminaPrimaryGlow else LuminaPrimary
                            ) else Modifier
                        )
                        .clickable {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = itemColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
