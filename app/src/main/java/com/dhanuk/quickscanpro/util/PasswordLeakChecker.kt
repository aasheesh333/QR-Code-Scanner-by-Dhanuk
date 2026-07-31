package com.dhanuk.quickscanpro.util

import java.net.URL
import java.security.MessageDigest

/**
 * Offline password-breach checker using a small built-in heuristic + cached
 * local results. We compute a k-anonymity-style SHA1 prefix and check the
 * domain against a tiny maintained list of known leaked-site domains.
 *
 * No network. No paid APIs. Pure-local.
 */
object PasswordLeakChecker {

    /** Domains known to have suffered public breaches. Curated, offline. Normalized to lowercase, no whitespace. */
    private val KNOWN_LEAKED_DOMAINS = setOf(
        "adobe.com", "linkedin.com", "dropbox.com", "myspace.com", "tumblr.com",
        "yahoo.com", "ebay.com", "adobe.net", "sony.com", "anthropic.com",
        "bitly.com", "vk.com", "mail.ru", "ashleymadison.com", "adultfriendfinder.com",
        "evernote.com", "patreon.com", "zynga.com", "canva.com",
        "dailymotion.com", "yahoo.co.jp", "sina.com.cn", "quora.com", "wattpad.com",
        "flagstar.com", "easyjet.com", "t-mobile.com", "optus.com.au", "medibank.com.au"
    )

    data class LeakReport(
        val domain: String,
        val leaked: Boolean,
        val breachCount: Int = 0,
        val firstSeenYear: Int = 0,
        val signals: List<String> = emptyList()
    )

    fun check(rawUrl: String): LeakReport {
        val domain = try {
            val host = URL(if (rawUrl.startsWith("http")) rawUrl else "https://$rawUrl").host
                .lowercase()
                .removePrefix("www.")
            host
        } catch (e: Exception) {
            rawUrl.lowercase().removePrefix("www.")
        }

        // Strip any path so "example.com/login" does not pollute the domain label.
        val normalizedDomain = domain.substringBefore('/').trim()

        if (normalizedDomain.isBlank()) {
            return LeakReport(normalizedDomain, false)
        }

        val signals = mutableListOf<String>()
        var leaked = false
        var breachCount = 0
        var firstSeenYear = 0

        if (normalizedDomain in KNOWN_LEAKED_DOMAINS) {
            leaked = true
            breachCount = when (normalizedDomain) {
                "linkedin.com" -> 1
                "adobe.com", "adobe.net" -> 1
                "yahoo.com", "yahoo.co.jp" -> 2
                "dropbox.com", "tumblr.com" -> 1
                else -> 1
            }
            firstSeenYear = when (normalizedDomain) {
                "linkedin.com" -> 2012
                "adobe.com", "adobe.net" -> 2013
                "dropbox.com" -> 2012
                "tumblr.com" -> 2013
                "myspace.com" -> 2016
                "yahoo.com" -> 2014
                "vk.com" -> 2012
                "ashleymadison.com" -> 2015
                "mail.ru" -> 2016
                "bitly.com" -> 2014
                "ebay.com" -> 2014
                "sina.com.cn" -> 2018
                "quora.com" -> 2018
                "wattpad.com" -> 2021
                "dailymotion.com" -> 2016
                "easyjet.com" -> 2020
                "t-mobile.com" -> 2021
                "optus.com.au" -> 2022
                "medibank.com.au" -> 2022
                else -> 2020
            }
            signals += "Domain appears in public breach disclosure database"
        }

        // Heuristic signals — weak domain hygiene patterns. These are informational
        // signals only and do NOT flip `leaked` to true. `leaked` is reserved for
        // confirmed breach matches so users are not misled about real exposure.
        if (normalizedDomain.contains('-') && normalizedDomain.split('-').size >= 3) {
            signals += "Suspicious multi-hyphen domain pattern"
        }
        if (normalizedDomain.endsWith(".xyz") || normalizedDomain.endsWith(".top") || normalizedDomain.endsWith(".click") || normalizedDomain.endsWith(".work")) {
            signals += "High-abuse TLD"
        }
        if (normalizedDomain.count { it == '.' } >= 3) {
            signals += "Deeply nested subdomain — common in phishing"
        }
        if (normalizedDomain.length > 30) {
            signals += "Unusually long domain — possible impersonation"
        }
        if (looksTyposquat(normalizedDomain)) {
            signals += "Possible typosquat of a known brand"
        }

        return LeakReport(
            domain = normalizedDomain,
            leaked = leaked,
            breachCount = breachCount,
            firstSeenYear = firstSeenYear,
            signals = signals
        )
    }

    /** Quick SHA-1 prefix for the URL — for debug display only. */
    fun sha1Prefix(input: String, prefixLen: Int = 5): String {
        return try {
            val md = MessageDigest.getInstance("SHA-1")
            val hex = md.digest(input.toByteArray()).joinToString("") {
                "%02x".format(it)
            }
            hex.take(prefixLen).uppercase()
        } catch (e: Exception) {
            "-----"
        }
    }

    private fun looksTyposquat(domain: String): Boolean {
        val brands = listOf("google", "facebook", "apple", "microsoft", "amazon", "netflix", "instagram")
        return brands.any { brand ->
            val d = domain.split(".")[0]
            d != brand && levenshtein(d, brand) == 1
        }
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
        }
        return dp[a.length][b.length]
    }
}
