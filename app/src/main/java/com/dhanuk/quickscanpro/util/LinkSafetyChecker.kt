package com.dhanuk.quickscanpro.util

import android.net.Uri

/**
 * Offline heuristic link-safety analyzer — a feature most scanner
 * apps don't ship. Scores a URL and explains the signals in plain
 * language so users can spot phishing before opening a link.
 */
object LinkSafetyChecker {

    enum class Level { SAFE, CAUTION, RISKY, NOT_A_LINK }

    data class Report(
        val level: Level,
        val score: Int,               // 0 (dangerous) .. 100 (very safe)
        val signals: List<String>
    )

    private val shorteners = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly", "is.gd",
        "buff.ly", "cutt.ly", "rebrand.ly", "shorturl.at", "tiny.cc"
    )

    private val suspiciousTlds = listOf(
        ".xyz", ".top", ".club", ".work", ".click", ".link", ".loan",
        ".win", ".bid", ".stream", ".download", ".racing", ".review"
    )

    private val phishingKeywords = listOf(
        "login", "verify", "secure-", "account-", "update-", "confirm",
        "banking", "password", "signin", "wallet", "airdrop", "gift",
        "free-", "prize", "winner", "claim"
    )

    fun analyze(content: String): Report {
        val trimmed = content.trim()
        val lower = trimmed.lowercase()
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return Report(Level.NOT_A_LINK, -1, emptyList())
        }

        val uri = try { Uri.parse(trimmed) } catch (e: Exception) { null }
        val host = uri?.host?.lowercase() ?: ""
        val signals = mutableListOf<String>()
        var score = 100

        // 1. Scheme
        if (lower.startsWith("http://")) {
            score -= 25
            signals.add("Not encrypted (HTTP) — data can be intercepted")
        } else {
            signals.add("Encrypted connection (HTTPS)")
        }

        // 2. IP address as host
        if (host.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}$"))) {
            score -= 30
            signals.add("Uses a raw IP address instead of a domain name")
        }

        // 3. URL shortener
        if (shorteners.any { host == it || host.endsWith(".$it") }) {
            score -= 20
            signals.add("Shortened link — the real destination is hidden")
        }

        // 4. Suspicious TLD
        val tld = suspiciousTlds.firstOrNull { host.endsWith(it) }
        if (tld != null) {
            score -= 15
            signals.add("Uncommon domain ending ($tld) often used by spam sites")
        }

        // 5. Phishing keywords in host/path
        val fullPath = (host + (uri?.path ?: "")).lowercase()
        val hits = phishingKeywords.filter { fullPath.contains(it) }
        if (hits.isNotEmpty()) {
            score -= 10 * hits.size.coerceAtMost(3)
            signals.add("Contains bait words: ${hits.joinToString(", ")}")
        }

        // 6. Excessive subdomains
        val dots = host.count { it == '.' }
        if (dots >= 3) {
            score -= 10
            signals.add("Many subdomains — can disguise the real domain")
        }

        // 7. '@' in URL (credential trick)
        if (trimmed.contains('@')) {
            score -= 20
            signals.add("Contains '@' — may redirect to an unexpected site")
        }

        // 8. Very long URL
        if (trimmed.length > 120) {
            score -= 5
            signals.add("Unusually long link")
        }

        // 9. APK / exe download — classic fake-QR / malicious payload
        if (lower.endsWith(".apk") || lower.contains("/download/") && lower.contains(".apk") ||
            lower.endsWith(".exe") || lower.endsWith(".dll")) {
            score -= 35
            signals.add("WARNING: Links to an app/installer download (.apk/.exe) — high malware risk")
        }

        // 10. Clipboard-injection payload — some QR Codes carry `javascript:` or shell commands
        if (lower.startsWith("javascript:") || lower.contains("data:text/html") ||
            lower.contains("intent://") || lower.contains("\\x1b[")) {
            score -= 30
            signals.add("Suspicious embedded script/intent payload — could inject actions")
        }

        // 11. Multiple redirect parameters — `?url=`, `?redirect=`, `?to=`
        if (lower.contains("redirect=") || lower.contains("?url=") || lower.contains("?to=") ||
            lower.contains("&next=") || lower.contains("returnurl=")) {
            score -= 15
            signals.add("Contains a redirect parameter — common phishing pattern")
        }

        if (signals.size == 1 && signals.first().startsWith("Encrypted")) {
            signals.add("No warning signs detected")
        }

        score = score.coerceIn(0, 100)
        val level = when {
            score >= 75 -> Level.SAFE
            score >= 45 -> Level.CAUTION
            else -> Level.RISKY
        }
        return Report(level, score, signals)
    }
}
