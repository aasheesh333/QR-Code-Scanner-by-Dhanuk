package com.dhanuk.quickscanpro

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dhanuk.quickscanpro.ads.InterstitialAdManager
import com.dhanuk.quickscanpro.ui.screens.MainScreen
import com.dhanuk.quickscanpro.ui.theme.QuickScanProTheme
import com.dhanuk.quickscanpro.viewmodel.ThemeViewModel

class MainActivity : AppCompatActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        InterstitialAdManager.loadAd(this)
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState()
            QuickScanProTheme(themeMode = themeMode) {
                MainScreen()
            }
        }
    }
}
