package com.dhanuk.quickscanpro.util

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build

/**
 * One-tap "share my WiFi" — reads the currently connected network's
 * SSID so the user can instantly generate a QR for guests. Getting
 * the password is impossible on stock Android (only root), so the
 * QR is generated with an empty password field for the user to fill
 * in once — still far faster than typing the SSID by hand.
 */
object WifiShareHelper {

    data class CurrentWifi(val ssid: String)

    fun getCurrentWifi(context: Context): CurrentWifi? {
        return try {
            val wm = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wm.isWifiEnabled) return null
            val info = wm.connectionInfo ?: return null
            var ssid = info.ssid ?: return null
            // Strip surrounding quotes Android adds
            ssid = ssid.removePrefix("\"").removeSuffix("\"")
            if (ssid == "<unknown ssid>" || ssid.isBlank()) null
            else CurrentWifi(ssid)
        } catch (e: Exception) {
            null
        }
    }
}
