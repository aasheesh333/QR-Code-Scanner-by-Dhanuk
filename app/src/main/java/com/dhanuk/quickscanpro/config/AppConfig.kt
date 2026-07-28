package com.dhanuk.quickscanpro.config

import com.dhanuk.quickscanpro.BuildConfig

object AppConfig {
    const val APP_NAME = "QuickScan Pro"

    object AdMob {
        const val BANNER_AD_UNIT_ID_HOME = BuildConfig.BANNER_AD_ID
        const val BANNER_AD_UNIT_ID_HISTORY = BuildConfig.BANNER_AD_ID
        const val INTERSTITIAL_AD_UNIT_ID = BuildConfig.INTERSTITIAL_AD_ID
    }

    object OneSignal {
        const val APP_ID = BuildConfig.ONESIGNAL_APP_ID
    }
}
