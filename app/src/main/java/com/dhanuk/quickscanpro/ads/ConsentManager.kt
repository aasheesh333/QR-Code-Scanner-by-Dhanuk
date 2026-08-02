package com.dhanuk.quickscanpro.ads

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object ConsentManager {

    private var consentInformation: ConsentInformation? = null

    fun requestConsent(activity: Activity, onResult: (canShowAds: Boolean) -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        val info = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation = info

        // If consent has already been obtained and ads can be requested, skip form
        if (info.canRequestAds()) {
            onResult(true)
            return
        }

        info.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadError ->
                    if (loadError != null) {
                        onResult(false)
                    } else {
                        onResult(info.canRequestAds())
                    }
                }
            },
            { requestConsentError ->
                onResult(false)
            }
        )
    }

    fun canRequestAds(activity: Activity): Boolean {
        return consentInformation?.canRequestAds()
            ?: UserMessagingPlatform.getConsentInformation(activity).canRequestAds()
    }
}
