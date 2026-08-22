package com.dhanuk.quickscanpro.config

import com.dhanuk.quickscanpro.BuildConfig

object AppConfig {
    const val APP_NAME = "QuickScan Pro"
    const val SUPPORT_EMAIL = "support@dhanuksoftwares.com"
    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.dhanuk.quickscanpro"
    const val WEBSITE_URL = "https://dhanuk.page.gd/QuickScan-Pro/"
    val SHARE_TEXT = """
Check out QuickScan Pro — the fastest QR & barcode scanner I've used.

* Instant scanning for QR codes, links, Wi-Fi, contacts & more
* Generate and share Wi-Fi QR codes so friends connect in one scan
* Bulk scan & export to Excel/CSV, secure vault for private scans
* Password security check, compare mode, dark theme — all free & ad-light

Download: $PLAY_STORE_URL
Website: $WEBSITE_URL
""".trimIndent()

    object AdMob {
        const val BANNER_AD_UNIT_ID_HOME = BuildConfig.BANNER_AD_ID
        const val BANNER_AD_UNIT_ID_HISTORY = BuildConfig.BANNER_AD_ID
        const val INTERSTITIAL_AD_UNIT_ID = BuildConfig.INTERSTITIAL_AD_ID
        const val REWARDED_AD_UNIT_ID = BuildConfig.REWARDED_AD_ID
    }

    object OneSignal {
        const val APP_ID = BuildConfig.ONESIGNAL_APP_ID
    }

    object Legal {
        const val BASE_URL = "https://dhanuk.page.gd/QuickScan-Pro"
        const val PRIVACY_POLICY = "$BASE_URL/privacy-policy.html"
        const val TERMS = "$BASE_URL/terms.html"
        const val ABOUT_US = "$BASE_URL/about-us.html"
        const val CONTACT_US = "$BASE_URL/contact-us.html"
        const val PERMISSIONS = "$BASE_URL/permissions.html"
    }
}
