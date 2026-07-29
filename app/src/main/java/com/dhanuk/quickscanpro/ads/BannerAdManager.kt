package com.dhanuk.quickscanpro.ads

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object BannerAdManager {

    private var adView: AdView? = null
    private var retryJob: Job? = null
    private var retryCount = 0
    private val maxRetries = 5
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        MobileAds.initialize(context) { }
        isInitialized = true
    }

    fun getAdView(context: Context, adUnitId: String): AdView {
        init(context)
        adView?.let { existing ->
            if (existing.adUnitId == adUnitId && existing.parent != null) {
                return existing
            }
            (existing.parent as? ViewGroup)?.removeView(existing)
            existing.destroy()
        }

        val newAdView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    retryCount = 0
                    retryJob?.cancel()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    retryJob?.cancel()
                    if (retryCount < maxRetries) {
                        retryCount++
                        val delayMs = (3000L * retryCount).coerceAtMost(15000L)
                        retryJob = CoroutineScope(Dispatchers.Main).launch {
                            delay(delayMs)
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                }
            }
            loadAd(AdRequest.Builder().build())
        }
        adView = newAdView
        return newAdView
    }

    fun destroy() {
        retryJob?.cancel()
        adView?.destroy()
        adView = null
        isInitialized = false
        retryCount = 0
    }
}
