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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
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
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ui.navigation.BottomNavItem
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.QRGeneratorViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel

private fun encode(data: String) = android.net.Uri.encode(data)

private val ScreenWidthCap = 480.dp

/**
 * Navigation already URI-decodes path arguments once, so the value returned by
 * `arguments.getString(...)` is the original content. Decoding again here (e.g.
 * with URLDecoder) double-decodes and corrupts payloads — `+` becomes a space,
 * `%XX` sequences get mangled. Keep this as identity.
 */
private fun decode(data: String): String = data

private fun handleDefaultScanAction(context: Context, content: String, action: String, canSave: Boolean) {
    when (action) {
        "copy_clipboard" -> {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText("scan", content))
            Toast.makeText(context, "Scan copied", Toast.LENGTH_SHORT).show()
        }
        "share" -> {
            runCatching {
                context.startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, content)
                    },
                    "Share scan"
                ))
            }
        }
        "open_url" -> {
            if (BarcodeTypeDetector.detectType(content) == BarcodeTypeDetector.TYPE_URL) {
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(content))) }
                    .onFailure { Toast.makeText(context, "No app can open this link", Toast.LENGTH_SHORT).show() }
            } else if (canSave) {
                Toast.makeText(context, "Saved to history", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Scanned (history off)", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val settingsVm: SettingsViewModel = viewModel()
    val historyVm: HistoryViewModel = viewModel()
    val qrGeneratorVm: QRGeneratorViewModel = viewModel()
    val onboardingCompleted by settingsVm.onboardingCompleted.collectAsState()
    val defaultAction by settingsVm.defaultAction.collectAsState()
    val autoCopy by settingsVm.autoCopyOnScan.collectAsState()
    val scanHistory by settingsVm.scanHistory.collectAsState()
    val incognito by settingsVm.incognitoMode.collectAsState()
    val canSave = scanHistory && !incognito

    when {
        onboardingCompleted == false -> {
            OnboardingScreen { settingsVm.completeOnboarding() }
        }
        else -> {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        com.dhanuk.quickscanpro.ui.composables.BannerAd(
                            adUnitId = com.dhanuk.quickscanpro.BuildConfig.BANNER_AD_ID,
                            modifier = Modifier.widthIn(max = ScreenWidthCap)
                        )
                        AppBottomBar(navController)
                    }
                }
            ) { inner ->
                Box(
                    modifier = Modifier
                        .padding(inner)
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(modifier = Modifier.fillMaxSize().widthIn(max = ScreenWidthCap)) {
                        AppNavigation(navController, context, defaultAction, autoCopy, canSave, historyVm, qrGeneratorVm)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController) {
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: return
    val prefix = route.split("/").first().substringBefore("?")
    if (BottomNavItem.entries.none { it.route.split("/").first() == prefix }) return

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 3.dp
    ) {
        BottomNavItem.entries.forEach { item ->
            val selected = route.split("/").first() == item.route
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
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

private fun NavHostController.bottomNav(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun AppNavigation(
    navController: NavHostController,
    context: Context,
    defaultAction: String,
    autoCopy: Boolean,
    canSave: Boolean,
    historyVm: HistoryViewModel,
    qrGeneratorVm: QRGeneratorViewModel
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
                        navController.navigate("result/${encode(result)}")
                    } else {
                        if (canSave) historyVm.addScanResult(ScanResult(content = result))
                        handleDefaultScanAction(context, result, defaultAction, canSave)
                    }
                },
                onViewAllHistory = { navController.bottomNav(BottomNavItem.History.route) },
                onOpenBatch = { navController.navigate("batch_scan") },
                onOpenCompare = { navController.navigate("compare_scan") },
                onOpenVault = { navController.navigate("vault") },
                onOpenTimeline = { navController.navigate("timeline") },
                onOpenTemplates = { navController.navigate("templates") },
                onOpenLeakCheck = { navController.navigate("leak_check") },
                onOpenAnalytics = { navController.navigate("analytics") },
                onOpenSettings = { navController.bottomNav(BottomNavItem.Settings.route) },
                onOpenGenerate = { navController.bottomNav(BottomNavItem.Generate.route) },
                onOpenTextScan = { navController.navigate("text_scan") },
                onOpenWifiShare = { navController.navigate("wifi_share") }
            )
        }
        composable(BottomNavItem.Generate.route) {
            QRGeneratorScreen(
                vm = qrGeneratorVm,
                onOpenSettings = { navController.bottomNav(BottomNavItem.Settings.route) },
                onOpenBulk = { navController.navigate("bulk_generate") },
                onOpenTemplates = { navController.navigate("templates") }
            )
        }
        composable(BottomNavItem.History.route) {
            HistoryScreen(
                onOpenVault = { navController.navigate("vault") },
                onOpenTimeline = { navController.navigate("timeline") },
                onNavigateToScanner = { navController.bottomNav(BottomNavItem.Home.route) },
                onRowClick = { scan -> navController.navigate("result/${encode(scan.content)}?fromHistory=true") }
            )
        }
        composable(BottomNavItem.Settings.route) {
            SettingsScreen(
                onNavigateToAbout = { navController.navigate("about") },
                onNavigateToVault = { navController.navigate("vault") },
                onNavigateToThemeStudio = { navController.navigate("theme_studio") },
                onNavigateToPermissions = { navController.navigate("permissions") },
                onNavigateToPrivacy = { navController.navigate("privacy") },
                onNavigateToTerms = { navController.navigate("terms") },
                onNavigateToContact = { navController.navigate("contact") }
            )
        }
        composable("analytics") { AnalyticsScreen { navController.popBackStack() } }
        composable(
            route = "result/{data}?fromHistory={fromHistory}",
            arguments = listOf(
                navArgument("data") { type = NavType.StringType },
                navArgument("fromHistory") { type = NavType.BoolType; defaultValue = false }
            )
        ) { entry ->
            val data = entry.arguments?.getString("data")?.let(::decode) ?: ""
            val fromHistory = entry.arguments?.getBoolean("fromHistory") ?: false
            ResultScreen(
                data = data,
                fromHistory = fromHistory,
                onNavigateBack = { navController.popBackStack() },
                onOpenProductLookup = { barcode -> navController.navigate("product_lookup/${encode(barcode)}") }
            )
        }
        composable("batch_scan") { BatchScanScreen { navController.popBackStack() } }
        composable("compare_scan") { CompareScanScreen { navController.popBackStack() } }
        composable("vault") { VaultScreen { navController.popBackStack() } }
        composable("theme_studio") { ThemeStudioScreen { navController.popBackStack() } }
        composable("timeline") {
            TimelineScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("templates") {
            TemplatesScreen(
                onNavigateBack = { navController.popBackStack() },
                onUseTemplate = { template ->
                    qrGeneratorVm.prefill(
                        type = template.type,
                        p1 = template.prefill.f1,
                        p2 = template.prefill.f2,
                        p3 = template.prefill.f3,
                        p4 = template.prefill.f4
                    )
                    Toast.makeText(context, "Template applied — fill the fields and generate", Toast.LENGTH_SHORT).show()
                    navController.bottomNav(BottomNavItem.Generate.route)
                }
            )
        }
        composable("leak_check") {
            LeakCheckScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenPasswordTools = { navController.navigate("password_tools") }
            )
        }
        composable("text_scan") {
            TextScanScreen(
                onNavigateBack = { navController.popBackStack() },
                onTextExtracted = { text ->
                    if (canSave) historyVm.addScanResult(ScanResult(content = text))
                    navController.navigate("result/${encode(text)}")
                }
            )
        }
        composable("wifi_share") {
            WifiShareScreen(
                onNavigateBack = { navController.popBackStack() },
                onShareReady = { content ->
                    if (canSave) historyVm.addScanResult(ScanResult(content = content))
                }
            )
        }
        composable("bulk_generate") { BulkGenerateScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("password_tools") { PasswordToolsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(
            route = "product_lookup/{barcode}",
            arguments = listOf(navArgument("barcode") { type = NavType.StringType })
        ) { entry ->
            val raw = entry.arguments?.getString("barcode") ?: ""
            ProductLookupScreen(
                barcode = decode(raw),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("about") { AboutScreen { navController.popBackStack() } }
        composable("permissions") { PermissionsUsageScreen { navController.popBackStack() } }
        composable("privacy") { PrivacyPolicyScreen { navController.popBackStack() } }
        composable("terms") { TermsAndConditionsScreen { navController.popBackStack() } }
        composable("contact") { ContactUsScreen { navController.popBackStack() } }
    }
}
