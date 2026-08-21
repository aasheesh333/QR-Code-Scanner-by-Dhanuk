package com.dhanuk.quickscanpro.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.dhanuk.quickscanpro.config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object InterstitialAdManager {

    private const val SCANS_BETWEEN_ADS = 4
    /** Show an interstitial at most once every 3 minutes of app usage. */
    private const val AUTO_SHOW_INTERVAL_MS = 3 * 60 * 1000L

    private var interstitialAd: InterstitialAd? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var retryCount = 0
    private val maxRetries = 3
    private var isLoading = false
    private var appContext: Context? = null

    private var scansSinceLastAd = 0
    private var lastAutoShowTime = 0L
    private var sessionStartTime = 0L

    fun loadAd(context: Context) {
        appContext = context.applicationContext
        if (sessionStartTime == 0L) sessionStartTime = System.currentTimeMillis()
        if (isLoading || interstitialAd != null) return
        isLoading = true

        InterstitialAd.load(
            appContext!!,
            AppConfig.AdMob.INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    if (retryCount < maxRetries) {
                        retryCount++
                        val delayMs = (5000L * retryCount).coerceAtMost(30000L)
                        scope.launch {
                            delay(delayMs)
                            appContext?.let { loadAd(it) }
                        }
                    }
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    retryCount = 0
                }
            }
        )
    }

    /** Call on every successful scan; shows an interstitial every N scans. */
    fun recordScan(context: Context) {
        if (RewardedAdManager.sessionAdFree) return
        appContext = context.applicationContext
        scansSinceLastAd++
        if (scansSinceLastAd >= SCANS_BETWEEN_ADS && interstitialAd != null) {
            scansSinceLastAd = 0
            showAd(context)
        } else if (interstitialAd == null) {
            loadAd(context)
        }
    }

    /** Show interstitial after completing a meaningful action (export, generate, etc.). */
    fun showAfterAction(context: Context) {
        if (RewardedAdManager.sessionAdFree) return
        appContext = context.applicationContext
        if (interstitialAd != null) {
            showAd(context)
        } else {
            loadAd(context)
        }
    }

    /**
     * Auto-show an interstitial after the user has been in the app for a while.
     * Call this from a lifecycle observer or a timer.
     */
    fun tryAutoShow(context: Context) {
        if (RewardedAdManager.sessionAdFree) return
        if (sessionStartTime == 0L) sessionStartTime = System.currentTimeMillis()
        val now = System.currentTimeMillis()
        if (now - lastAutoShowTime < AUTO_SHOW_INTERVAL_MS) return
        if (interstitialAd != null) {
            lastAutoShowTime = now
            showAd(context)
        } else {
            loadAd(context)
        }
    }

    fun showAd(context: Context) {
        if (RewardedAdManager.sessionAdFree) return
        appContext = context.applicationContext
        val activity = context as? Activity ?: return
        val ad = interstitialAd ?: return
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                appContext?.let { loadAd(it) }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                appContext?.let { loadAd(it) }
            }
        }
        ad.show(activity)
    }

    fun isAdReady(): Boolean = interstitialAd != null

    fun destroy() {
        interstitialAd = null
        isLoading = false
        retryCount = 0
        lastAutoShowTime = 0L
        sessionStartTime = 0L
    }
}
