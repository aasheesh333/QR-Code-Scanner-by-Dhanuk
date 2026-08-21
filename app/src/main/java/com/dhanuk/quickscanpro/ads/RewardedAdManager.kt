package com.dhanuk.quickscanpro.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.dhanuk.quickscanpro.config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages rewarded ads. Users can watch a short ad to unlock premium
 * features (e.g. remove ads for a session, unlock bulk generation limit,
 * or access a premium export format).
 */
object RewardedAdManager {

    private var rewardedAd: RewardedAd? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isLoading = false
    private var appContext: Context? = null
    private var retryCount = 0
    private const val MAX_RETRIES = 2

    /** Whether the user has removed ads for this session via rewarded ad. */
    var sessionAdFree = false
        private set

    fun loadAd(context: Context) {
        appContext = context.applicationContext
        if (isLoading || rewardedAd != null) return
        isLoading = true

        RewardedAd.load(
            appContext!!,
            AppConfig.AdMob.REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    if (retryCount < MAX_RETRIES) {
                        retryCount++
                        scope.launch {
                            delay(5000L * retryCount)
                            appContext?.let { loadAd(it) }
                        }
                    }
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    retryCount = 0
                }
            }
        )
    }

    fun isReady(): Boolean = rewardedAd != null

    /**
     * Shows a rewarded ad. Calls [onRewarded] when the user earns the reward,
     * or [onDismissed] if the ad is closed without earning.
     */
    fun show(
        activity: Activity,
        onRewarded: (RewardItem) -> Unit = {},
        onDismissed: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad == null) {
            onDismissed()
            appContext?.let { loadAd(it) }
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                appContext?.let { loadAd(it) }
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                appContext?.let { loadAd(it) }
                onDismissed()
            }
        }
        ad.show(activity) { reward ->
            onRewarded(reward)
        }
    }

    /**
     * Grant ad-free session after watching a rewarded ad.
     */
    fun grantAdFreeSession() {
        sessionAdFree = true
    }

    fun reset() {
        sessionAdFree = false
        rewardedAd = null
        isLoading = false
        retryCount = 0
    }
}
