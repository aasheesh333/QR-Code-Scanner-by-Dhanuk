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

/**
 * One-tap "share my WiFi" — reads the currently connected network's
 * SSID so the user can instantly generate a QR for guests.
 *
 * Requirements: ACCESS_FINE_LOCATION is needed to read the SSID, and
 * on Android 10+ location *services* must also be ON — otherwise Android
 * hands back `<unknown ssid>` and detection silently fails.
 *
 * NOTE: every method is defensive — all platform calls are wrapped in
 * try/catch(Throwable) so a misbehaving OEM ROM can never crash the app.
 */
object WifiShareHelper {

    data class CurrentWifi(val ssid: String)

    fun hasLocationPermission(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    fun isLocationServicesOn(context: Context): Boolean {
        return try {
            val lm = context.getSystemService(LocationManager::class.java) ?: return false
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Throwable) {
            false
        }
    }

    fun openLocationSettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    /** True if the device currently has an active Wi-Fi network. */
    fun isWifiEnabled(context: Context): Boolean {
        return try {
            val cm = context.applicationContext.getSystemService(ConnectivityManager::class.java)
                ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } catch (_: Throwable) {
            false
        }
    }

    fun getCurrentWifi(context: Context): CurrentWifi? {
        return try {
            if (!hasLocationPermission(context)) return null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isLocationServicesOn(context)) return null
            if (!isWifiEnabled(context)) return null
            readCurrentWifi(context)?.let(::sanitizeSsid)?.let(::CurrentWifi)
        } catch (_: Throwable) {
            null
        }
    }

    private fun readCurrentWifi(context: Context): String? {
        // ── Legacy path (API 23–28): WifiManager.connectionInfo is the only way. ──
        // getTransportInfo() does NOT exist below API 29 — calling it throws
        // NoSuchMethodError, which is why the version check must come FIRST.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return try {
                val wm = context.applicationContext.getSystemService(WifiManager::class.java)
                    ?: return null
                wm.connectionInfo?.ssid
            } catch (_: Throwable) {
                null
            }
        }

        // ── Modern path (API 29+): NetworkCapabilities.transportInfo exposes the
        // real SSID. WifiManager.connectionInfo is deprecated and often returns
        // <unknown ssid> on Android 12+.
        return try {
            val cm = context.applicationContext.getSystemService(ConnectivityManager::class.java)
                ?: return null
            val network = cm.activeNetwork ?: return null
            val caps = cm.getNetworkCapabilities(network) ?: return null
            if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
            val info = caps.transportInfo as? WifiInfo ?: return null
            info.ssid
        } catch (_: Throwable) {
            // OEM-specific SecurityException / dead object — fall back to legacy.
            try {
                val wm = context.applicationContext.getSystemService(WifiManager::class.java)
                    ?: return null
                wm.connectionInfo?.ssid
            } catch (_: Throwable) {
                null
            }
        }
    }

    private fun sanitizeSsid(raw: String): String? {
        val ssid = raw
            .removePrefix("\"")
            .removeSuffix("\"")
            .trim()
        return if (ssid.equals("<unknown ssid>", ignoreCase = true) || ssid.isBlank()) null else ssid
    }
}
