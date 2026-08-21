package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dhanuk.quickscanpro.ads.ConsentManager
import com.dhanuk.quickscanpro.ads.RewardedAdManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAd(adUnitId: String, modifier: Modifier = Modifier) {
    val allowed by ConsentManager.adsAllowed.collectAsState()
    if (!allowed || adUnitId.isBlank() || RewardedAdManager.sessionAdFree) return

    var loaded by remember(adUnitId) { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(adUnitId) {
        val observer = LifecycleEventObserver { _, event ->
            AdViewHolder.view?.let { view ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> view.pause()
                    Lifecycle.Event.ON_RESUME -> view.resume()
                    Lifecycle.Event.ON_DESTROY -> view.destroy()
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (loaded) 60.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() { loaded = true }
                        override fun onAdFailedToLoad(error: LoadAdError) { loaded = false }
                    }
                    loadAd(AdRequest.Builder().build())
                    AdViewHolder.view = this
                }
            },
            onRelease = { view ->
                if (AdViewHolder.view === view) AdViewHolder.view = null
                view.destroy()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private object AdViewHolder {
    var view: AdView? = null
}
