package com.dhanuk.quickscanpro

import android.app.Application
import com.dhanuk.quickscanpro.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class QuickScanProApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        initCrashlytics()
        initAds()
        // OneSignal is intentionally NOT started here — it collects device data, so it
        // is deferred until the UMP consent flow has resolved (see MainActivity).
    }

    private fun initCrashlytics() {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
            crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("build_type", if (BuildConfig.DEBUG) "debug" else "release")
        } catch (e: Exception) {
            // Crashlytics not available — don't crash the app
        }
    }

    private fun initAds() {
        // Ad initialization is now gated by UMP consent — see MainActivity
        // MobileAds.initialize() is called after ConsentManager grants permission
    }

    private var oneSignalStarted = false

    /** Starts OneSignal once, after consent has been resolved. Safe to call repeatedly. */
    fun initOneSignal() {
        if (oneSignalStarted) return
        try {
            val appId = BuildConfig.ONESIGNAL_APP_ID
            if (appId.isEmpty() || appId == "placeholder-onesignal-app-id") return
            oneSignalStarted = true

            OneSignal.Debug.logLevel = if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.WARN
            OneSignal.initWithContext(this, appId)

            appScope.launch {
                try {
                    OneSignal.Notifications.requestPermission(false)
                } catch (e: Exception) {
                    // Permission request failed — non-fatal
                }
            }
        } catch (e: Exception) {
            // OneSignal init failure — don't crash
        }
    }
}
