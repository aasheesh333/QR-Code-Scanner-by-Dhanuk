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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object InterstitialAdManager {

    private var interstitialAd: InterstitialAd? = null
    private var retryJob: Job? = null
    private var retryCount = 0
    private val maxRetries = 3
    private var isLoading = false

    fun loadAd(context: Context) {
        if (isLoading) return
        isLoading = true
        retryJob?.cancel()

        InterstitialAd.load(
            context,
            AppConfig.AdMob.INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    if (retryCount < maxRetries) {
                        retryCount++
                        val delayMs = (5000L * retryCount).coerceAtMost(30000L)
                        retryJob = CoroutineScope(Dispatchers.Main).launch {
                            delay(delayMs)
                            loadAd(context)
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

    fun showAd(context: Context) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadAd(context)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                loadAd(context)
            }
        }
        (context as? Activity)?.let {
            interstitialAd?.show(it)
        }
    }

    fun isAdReady(): Boolean = interstitialAd != null

    fun destroy() {
        retryJob?.cancel()
        interstitialAd = null
        isLoading = false
        retryCount = 0
    }
}
