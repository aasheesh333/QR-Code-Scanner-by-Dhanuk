package com.dhanuk.quickscanpro.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * One-tap "share my WiFi" — reads the currently connected network's
 * SSID so the user can instantly generate a QR for guests. Getting
 * the password is impossible on stock Android (only root), so the
 * QR is generated with an empty password field for the user to fill
 * in once — still far faster than typing the SSID by hand.
 *
 * Note: On Android 10+ (API 29+), ACCESS_FINE_LOCATION is required
 * to read the SSID. Call [hasLocationPermission] before invoking
 * [getCurrentWifi] to provide a graceful UX.
 */
object WifiShareHelper {

    data class CurrentWifi(val ssid: String)

    fun hasLocationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCurrentWifi(context: Context): CurrentWifi? {
        if (!hasLocationPermission(context)) return null
        return try {
            val wm = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wm.isWifiEnabled) return null
            val info = wm.connectionInfo ?: return null
            var ssid = info.ssid ?: return null
            ssid = ssid.removePrefix("\"").removeSuffix("\"")
            if (ssid == "<unknown ssid>" || ssid.isBlank()) null
            else CurrentWifi(ssid)
        } catch (e: Exception) {
            null
        }
    }
}
