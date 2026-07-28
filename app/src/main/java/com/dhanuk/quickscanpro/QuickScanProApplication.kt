package com.dhanuk.quickscanpro

import android.app.Application
import com.dhanuk.quickscanpro.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuickScanProApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initOneSignal()
        initCrashlytics()
    }

    private fun initOneSignal() {
        val appId = BuildConfig.ONESIGNAL_APP_ID
        if (appId.isEmpty() || appId == "placeholder-onesignal-app-id") return

        OneSignal.Debug.logLevel = if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.WARN

        OneSignal.initWithContext(this, appId)

        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }
    }

    private fun initCrashlytics() {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(true)
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("build_type", if (BuildConfig.DEBUG) "debug" else "release")
    }
}
