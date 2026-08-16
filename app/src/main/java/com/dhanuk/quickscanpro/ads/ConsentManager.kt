package com.dhanuk.quickscanpro.ads

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ConsentManager {

    private var consentInformation: ConsentInformation? = null

    private val _adsAllowed = MutableStateFlow(false)
    val adsAllowed: StateFlow<Boolean> = _adsAllowed

    fun requestConsent(activity: Activity, onResult: (canShowAds: Boolean) -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        val info = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation = info

        // If consent has already been obtained and ads can be requested, skip form
        if (info.canRequestAds()) {
            _adsAllowed.value = true
            onResult(true)
            return
        }

        info.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadError ->
                    if (loadError != null) {
                        _adsAllowed.value = false
                        onResult(false)
                    } else {
                        _adsAllowed.value = info.canRequestAds()
                        onResult(info.canRequestAds())
                    }
                }
            },
            { requestConsentError ->
                _adsAllowed.value = false
                onResult(false)
            }
        )
    }

    fun canRequestAds(activity: Activity): Boolean {
        return consentInformation?.canRequestAds()
            ?: UserMessagingPlatform.getConsentInformation(activity).canRequestAds()
    }
}
