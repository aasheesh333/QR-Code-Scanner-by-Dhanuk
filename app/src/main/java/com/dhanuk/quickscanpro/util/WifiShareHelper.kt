package com.dhanuk.quickscanpro.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

/**
 * One-tap "share my WiFi" — reads the currently connected network's
 * SSID so the user can instantly generate a QR for guests. Getting
 * the password is impossible on stock Android (only root), so the
 * QR is generated with an empty password field for the user to fill
 * in once — still far faster than typing the SSID by hand.
 *
 * Requirements: ACCESS_FINE_LOCATION is needed to read the SSID, and
 * on Android 10+ location *services* must also be ON — otherwise Android
 * hands back `<unknown ssid>` and detection silently fails. Some OEM
 * skins hide the SSID even with everything granted; the screen lets
 * the user type the SSID manually in that case.
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

    fun isLocationServicesOn(context: Context): Boolean {
        val lm = context.getSystemService<LocationManager>() ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun openLocationSettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun isWifiEnabled(context: Context): Boolean {
        return try {
            val cm = context.applicationContext.getSystemService<ConnectivityManager>() ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } catch (_: Exception) {
            false
        }
    }

    fun getCurrentWifi(context: Context): CurrentWifi? {
        if (!hasLocationPermission(context)) return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isLocationServicesOn(context)) return null
        if (!isWifiEnabled(context)) return null
        return readCurrentWifi(context)?.let(::sanitizeSsid)?.let(::CurrentWifi)
    }

    private fun readCurrentWifi(context: Context): String? {
        val cm = context.applicationContext.getSystemService<ConnectivityManager>() ?: return null
        val network = cm.activeNetwork ?: return null
        val caps = runCatching { cm.getNetworkCapabilities(network) }.getOrNull() ?: return null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null

        // Modern path: NetworkCapabilities.transportInfo exposes the real SSID
        // on Android 10+ (WifiManager.connectionInfo is deprecated and often
        // returns <unknown ssid> on Android 12+).
        val info = caps.transportInfo
        if (info is WifiInfo) {
            return runCatching { info.ssid }.getOrNull()
        }

        // Legacy fallback for older devices / OEMs that don't fill transportInfo.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val wm = context.applicationContext.getSystemService<WifiManager>() ?: return null
            return runCatching { wm.connectionInfo?.ssid }.getOrNull()
        }
        return null
    }

    private fun sanitizeSsid(raw: String): String? {
        val ssid = raw
            .removePrefix("\"")
            .removeSuffix("\"")
            .trim()
        return if (ssid == "<unknown ssid>" || ssid.isBlank()) null else ssid
    }
}
