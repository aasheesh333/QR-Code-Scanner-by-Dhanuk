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
import kotlinx.coroutines.delay

/**
 * One-tap "share my WiFi" — reads the currently connected network's
 * SSID so the user can instantly generate a QR for guests.
 *
 * Reality on modern Android (researched against platform docs, AOSP
 * behaviour and OEM reports, Aug 2026):
 *
 * 1. Android 10+ gates the SSID behind location: ACCESS_FINE_LOCATION
 *    (or COARSE on most ROMs) **and** location services must be ON,
 *    otherwise the platform hands back `<unknown ssid>`.
 * 2. Even then there is no single reliable API:
 *      - `NetworkCapabilities.transportInfo` (API 29+) is the modern path
 *        but several OEM ROMs return `<unknown ssid>` there;
 *      - `WifiManager.connectionInfo` is deprecated on 29+ yet *does*
 *        work on many devices — so we try both;
 *      - the most robust fallback is BSSID matching: read the connected
 *        BSSID (often available even when the SSID is redacted) and look
 *        it up in `WifiManager.scanResults`.
 * 3. Android 13 (API 33)+ added NEARBY_WIFI_DEVICES with the
 *    `neverForLocation` flag, which unlocks scan results without any
 *    location permission — we use it as an extra source.
 *
 * Every method is defensive — all platform calls are wrapped in
 * try/catch(Throwable) so a misbehaving OEM ROM can never crash the app.
 */
object WifiShareHelper {

    data class CurrentWifi(val ssid: String)

    /** Permissions worth requesting on this device to unlock SSID reading. */
    fun permissionsToRequest(): Array<String> {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        return list.toTypedArray()
    }

    fun hasLocationPermission(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
            val fine = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            fine || coarse
        } catch (_: Throwable) {
            false
        }
    }

    /** API 33+ scan permission that does NOT need location services. */
    fun hasNearbyWifiPermission(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    /** Any permission that lets us attempt SSID detection at all. */
    fun hasAnyDetectionPermission(context: Context): Boolean {
        return hasLocationPermission(context) || hasNearbyWifiPermission(context)
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

    /** True if the Wi-Fi radio is on (regardless of being connected). */
    fun isWifiRadioOn(context: Context): Boolean {
        return try {
            val wm = context.applicationContext.getSystemService(WifiManager::class.java)
                ?: return false
            wm.isWifiEnabled
        } catch (_: Throwable) {
            false
        }
    }

    /** True if the device currently has an active Wi-Fi network. */
    fun isWifiEnabled(context: Context): Boolean {
        return try {
            if (!isWifiRadioOn(context)) return false
            findWifiCaps(context) != null
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Finds NetworkCapabilities for ANY connected Wi-Fi network — not just the
     * default "active" one. On dual-SIM phones (common on Android 9/10), mobile
     * data is often the default network while Wi-Fi stays connected in parallel,
     * and `activeNetwork` alone then reports "no Wi-Fi".
     */
    @Suppress("DEPRECATION")
    private fun findWifiCaps(context: Context): NetworkCapabilities? {
        return try {
            val cm = context.applicationContext.getSystemService(ConnectivityManager::class.java)
                ?: return null
            // Primary: the default network (covers single-stack devices too).
            cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
                ?.takeIf { it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) }
                ?.let { return it }
            // Fallback: all known networks (parallel Wi-Fi + LTE, captive portals).
            for (network in cm.allNetworks ?: return null) {
                val caps = cm.getNetworkCapabilities(network) ?: continue
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return caps
            }
            null
        } catch (_: Throwable) {
            null
        }
    }

    fun getCurrentWifi(context: Context): CurrentWifi? {
        return try {
            if (!hasAnyDetectionPermission(context)) return null
            if (!isWifiEnabled(context)) return null

            // Try every source and take the first real SSID. Each source
            // guards itself (redacted <unknown ssid> → null), so a source the
            // device blocks is simply skipped rather than aborting detection.
            val candidates = listOf(
                { readTransportInfoSsid(context) },
                { readConnectionInfoSsid(context) },
                { readConfiguredNetworkSsid(context) },
                { matchSsidFromScan(context) }
            )
            for (candidate in candidates) {
                val ssid = candidate()?.let(::sanitizeSsid)
                if (ssid != null) return CurrentWifi(ssid)
            }
            null
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Detection can lag behind the system by a moment after a network connects
     * or permissions are granted. Try a few times and request one fresh scan;
     * this avoids false "not detected" states on Xiaomi/Oppo/Vivo/Realme ROMs.
     */
    suspend fun getCurrentWifiWithRetries(
        context: Context,
        attempts: Int = 5,
        delayMs: Long = 350
    ): CurrentWifi? {
        repeat(attempts) { attempt ->
            getCurrentWifi(context)?.let { return it }
            if (attempt < attempts - 1) {
                requestFreshScan(context)
                delay(delayMs)
            }
        }
        return null
    }

    /** Best-effort scan refresh. Android may throttle it; false is safe to ignore. */
    @Suppress("DEPRECATION")
    private fun requestFreshScan(context: Context) {
        runCatching {
            context.applicationContext.getSystemService(WifiManager::class.java)?.startScan()
        }
    }

    /** API 29+ modern path: NetworkCapabilities.transportInfo (any Wi-Fi network). */
    private fun readTransportInfoSsid(context: Context): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return try {
            val caps = findWifiCaps(context) ?: return null
            val info = caps.transportInfo as? WifiInfo ?: return null
            info.ssid
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Legacy path — the only one below API 29, and still a valid fallback on
     * newer versions where some OEMs return a real SSID here but `<unknown
     * ssid>` from transportInfo (seen on various MIUI/ColorOS builds).
     */
    private fun readConnectionInfoSsid(context: Context): String? {
        return try {
            val wm = context.applicationContext.getSystemService(WifiManager::class.java)
                ?: return null
            wm.connectionInfo?.ssid
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Some OEM builds redact WifiInfo.ssid but still expose the configured
     * network matching connectionInfo.networkId. Use it when available.
     */
    @Suppress("DEPRECATION")
    private fun readConfiguredNetworkSsid(context: Context): String? {
        return try {
            val wm = context.applicationContext.getSystemService(WifiManager::class.java)
                ?: return null
            val networkId = wm.connectionInfo?.networkId ?: return null
            wm.configuredNetworks
                ?.firstOrNull { it.networkId == networkId }
                ?.SSID
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Most robust fallback: the connected BSSID is usually available even when
     * the SSID string is redacted — match it against scan results. This is the
     * approach recommended for Android 13+ (works with NEARBY_WIFI_DEVICES
     * alone, no location needed).
     */
    private fun matchSsidFromScan(context: Context): String? {
        return try {
            val wm = context.applicationContext.getSystemService(WifiManager::class.java)
                ?: return null
            val info = wm.connectionInfo ?: return null
            val bssid = info.bssid ?: return null
            if (bssid.equals("02:00:00:00:00:00", ignoreCase = true)) return null
            val results = wm.scanResults ?: return null
            results.firstOrNull { it.BSSID.equals(bssid, ignoreCase = true) }
                ?.SSID
        } catch (_: Throwable) {
            null
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
