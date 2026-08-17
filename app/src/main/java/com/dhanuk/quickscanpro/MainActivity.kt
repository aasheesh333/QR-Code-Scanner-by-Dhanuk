package com.dhanuk.quickscanpro

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dhanuk.quickscanpro.ads.ConsentManager
import com.dhanuk.quickscanpro.ads.InterstitialAdManager
import com.dhanuk.quickscanpro.ui.screens.MainScreen
import com.dhanuk.quickscanpro.ui.theme.QuickScanProTheme
import com.dhanuk.quickscanpro.util.VoiceSpeaker
import com.dhanuk.quickscanpro.viewmodel.ThemeViewModel
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : AppCompatActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Warm up the TTS engine so "Speak aloud" works instantly on first tap.
        VoiceSpeaker.init(this)

        // Gate ads + analytics behind UMP/GDPR consent
        ConsentManager.requestConsent(this) { canShowAds ->
            if (canShowAds) {
                try { MobileAds.initialize(this) } catch (_: Exception) {}
                InterstitialAdManager.loadAd(this)
            }
            runCatching {
                FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(canShowAds)
            }
            // Start push messaging only after the consent decision has resolved.
            (application as? QuickScanProApplication)?.initOneSignal()
        }

        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState()
            QuickScanProTheme(themeMode = themeMode) {
                MainScreen()
            }
        }
    }
}
