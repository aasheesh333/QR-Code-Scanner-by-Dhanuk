package com.dhanuk.quickscanpro.ui.composables

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.dhanuk.quickscanpro.ads.BannerAdManager

@Composable
fun BannerAd(adUnitId: String) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            FrameLayout(context).apply {
                val adView = BannerAdManager.getAdView(context, adUnitId)
                removeAllViews()
                addView(adView)
            }
        }
    )
}
