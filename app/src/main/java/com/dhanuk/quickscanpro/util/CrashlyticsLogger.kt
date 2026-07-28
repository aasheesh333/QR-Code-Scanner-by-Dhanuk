package com.dhanuk.quickscanpro.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsLogger {

    private val crashlytics: FirebaseCrashlytics?
        get() = try {
            FirebaseCrashlytics.getInstance()
        } catch (_: Exception) {
            null
        }

    fun logEvent(event: String) {
        crashlytics?.log(event)
    }

    fun logScanSuccess(contentType: String) {
        logEvent("scan_success: type=$contentType")
    }

    fun logScanFailure(reason: String) {
        logEvent("scan_failure: reason=$reason")
    }

    fun logQRGenerated(qrType: String) {
        logEvent("qr_generated: type=$qrType")
    }

    fun logAdShown(adType: String) {
        logEvent("ad_shown: type=$adType")
    }

    fun logAdFailed(adType: String, reason: String) {
        logEvent("ad_failed: type=$adType reason=$reason")
    }

    fun logException(e: Throwable, context: String? = null) {
        if (context != null) crashlytics?.log(context)
        crashlytics?.recordException(e)
    }

    fun setUserId(userId: String) {
        crashlytics?.setUserId(userId)
    }

    fun setCustomKey(key: String, value: String) {
        crashlytics?.setCustomKey(key, value)
    }
}
