package com.dhanuk.quickscanpro.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dhanuk.quickscanpro.ui.navigation.BottomNavItem
import com.dhanuk.quickscanpro.viewmodel.QRGeneratorViewModel
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private fun handleDefaultScanAction(context: Context, content: String, action: String) {
    when (action) {
        "copy_clipboard" -> {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText("scan", content))
            Toast.makeText(context, "Scan copied", Toast.LENGTH_SHORT).show()
        }
        "share" -> {
            context.startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, content)
                },
                "Share scan"
            ))
        }
        "open_url" -> {
            if (BarcodeTypeDetector.detectType(content) == BarcodeTypeDetector.TYPE_URL) {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(content)))
                }.onFailure {
                    Toast.makeText(context, "No app can open this link", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Saved to history", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
    val onboardingCompleted by settingsViewModel.onboardingCompleted.collectAsState()
    val defaultAction by settingsViewModel.defaultAction.collectAsState()
    val autoSave by settingsViewModel.autoCopyOnScan.collectAsState()

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
                bottomBar = { AppBottomBar(navController) }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AppNavigation(
                        navController = navController,
                        context = context,
                        defaultAction = defaultAction,
                        autoCopy = autoSave,
                        historyViewModel = historyViewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: return
    val routePrefix = currentRoute.split("/").first().substringBefore("?")
    val showBarPrefixes = BottomNavItem.entries.map { it.route.split("/").first() }
    if (!showBarPrefixes.contains(routePrefix)) return

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 3.dp
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
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun AppNavigation(
    navController: NavHostController,
    context: Context,
    defaultAction: String,
    autoCopy: Boolean,
    historyViewModel: HistoryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        enterTransition = { slideInHorizontally { it / 2 } + fadeIn() },
        exitTransition = { slideOutHorizontally { -it / 3 } + fadeOut() },
        popEnterTransition = { slideInHorizontally { -it / 2 } + fadeIn() },
        popExitTransition = { slideOutHorizontally { it / 3 } + fadeOut() }
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                onScan = { result ->
                    if (autoCopy) {
                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("scan", result))
                        Toast.makeText(context, "Scan copied", Toast.LENGTH_SHORT).show()
                    }
                    if (defaultAction == "show_result") {
                        val encoded = URLEncoder.encode(result, StandardCharsets.UTF_8.toString())
                        navController.navigate("result/$encoded")
                    } else {
                        historyViewModel.addScanResult(ScanResult(content = result))
                        handleDefaultScanAction(context, result, defaultAction)
                    }
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
                onOpenAnalytics = { navController.navigate("analytics") },
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
        composable(BottomNavItem.Generate.route) {
            QRGeneratorScreen(onOpenSettings = {
                navController.navigate(BottomNavItem.Settings.route) {
                    launchSingleTop = true
                }
            })
        }
        composable(BottomNavItem.History.route) {
            HistoryScreen(
                onOpenVault = { navController.navigate("vault") },
                onOpenCompare = { navController.navigate("compare_scan") },
                onNavigateToScanner = {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(BottomNavItem.Settings.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onRowClick = { scan ->
                    val encoded = URLEncoder.encode(scan.content, StandardCharsets.UTF_8.toString())
                    navController.navigate("result/$encoded")
                }
            )
        }
        composable("analytics") {
            AnalyticsScreen()
        }
        composable(BottomNavItem.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate("about") },
                onNavigateToVault = { navController.navigate("vault") }
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
                onOpenProductLookup = { barcode ->
                    val encoded = URLEncoder.encode(barcode, StandardCharsets.UTF_8.toString())
                    navController.navigate("product_lookup/$encoded")
                }
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
    }
}
