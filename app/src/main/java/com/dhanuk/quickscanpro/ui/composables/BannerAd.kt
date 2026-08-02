package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAd(adUnitId: String, modifier: Modifier = Modifier) {
    if (adUnitId.isBlank()) {
        Box(modifier.height(0.dp).fillMaxWidth())
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val adView = remember { AdViewHolder.view }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> adView?.pause()
                Lifecycle.Event.ON_RESUME -> adView?.resume()
                Lifecycle.Event.ON_DESTROY -> adView?.destroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    adListener = object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            // Ad failed to load — hide the banner space
                            this@apply.visibility = android.view.View.GONE
                        }
                        override fun onAdLoaded() {
                            this@apply.visibility = android.view.View.VISIBLE
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                    AdViewHolder.view = this
                }
            },
            onRelease = { view ->
                AdViewHolder.view = null
                view.destroy()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private object AdViewHolder {
    var view: AdView? = null
}
