package com.quickscanpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.quickscanpro.ads.InterstitialAdManager
import com.quickscanpro.ui.screens.MainScreen
import com.quickscanpro.ui.screens.SplashScreen
import com.quickscanpro.ui.theme.QuickScanProTheme
import com.quickscanpro.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InterstitialAdManager.loadAd(this)
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            QuickScanProTheme(darkTheme = isDarkTheme) {
                var showSplashScreen by remember { mutableStateOf(true) }

                if (showSplashScreen) {
                    SplashScreen(onTimeout = { showSplashScreen = false })
                } else {
                    MainScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    QuickScanProTheme {
        MainScreen()
    }
}
