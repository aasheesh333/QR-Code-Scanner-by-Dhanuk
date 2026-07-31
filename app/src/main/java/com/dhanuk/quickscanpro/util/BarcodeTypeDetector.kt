package com.dhanuk.quickscanpro.util

object BarcodeTypeDetector {

    const val TYPE_URL = "url"
    const val TYPE_EMAIL = "email"
    const val TYPE_PHONE = "phone"
    const val TYPE_SMS = "sms"
    const val TYPE_WIFI = "wifi"
    const val TYPE_VCARD = "vcard"
    const val TYPE_CALENDAR = "calendar"
    const val TYPE_GEO = "geo"
    const val TYPE_TEXT = "text"
    const val TYPE_PRODUCT = "product"
    const val TYPE_UNKNOWN = "unknown"

    fun detectType(rawContent: String): String {
        val content = rawContent.trim()
        if (content.isEmpty()) return TYPE_UNKNOWN

        val lower = content.lowercase()

        if (lower.startsWith("http://") || lower.startsWith("https://")) return TYPE_URL
        if (lower.startsWith("mailto:")) return TYPE_EMAIL
        if (lower.startsWith("tel:")) return TYPE_PHONE
        if (lower.startsWith("sms:") || lower.startsWith("smsto:")) return TYPE_SMS
        if (lower.startsWith("wifi:")) return TYPE_WIFI
        if (lower.startsWith("begin:vcard")) return TYPE_VCARD
        if (lower.startsWith("begin:vevent")) return TYPE_CALENDAR
        if (lower.startsWith("geo:")) return TYPE_GEO

        // Email pattern simple check
        if (content.contains("@") && content.contains(".") && !content.contains(" ")) return TYPE_EMAIL

        // Product barcode (EAN-8/UPC-A/EAN-13/ITF-14): exactly 8, 12, 13, or 14 digits.
        // 9/10/11-digit strings are NOT valid product barcodes. Checked before the
        // phone pattern because pure-digit strings also match phone.
        if (content.matches(Regex("^\\d{8}$|^\\d{12,14}$"))) return TYPE_PRODUCT

        // Phone pattern
        if (content.matches(Regex("^\\+?[0-9\\-\\s()]{7,20}$"))) return TYPE_PHONE

        return TYPE_TEXT
    }

    // ---- WiFi parser ----
    data class WifiInfo(val ssid: String, val password: String, val encryption: String)

    fun parseWifi(rawContent: String): WifiInfo? {
        // WIFI:T:<auth>;S:<ssid>;P:<password>;H:<hidden>;;
        // Per the ZXing spec the fields may appear in any order. Each field
        // value may contain escaped chars (\; \, \: \\ \"). We scan field by
        // field using a tokenizer anchored at `;` boundaries (respecting
        // backslash escapes).
        if (!rawContent.trimStart().startsWith("WIFI:", ignoreCase = true)) return null
        val body = rawContent.trim().substringAfter(":").trimEnd(';')

        var ssid = ""
        var password = ""
        var encryption = ""
        val tokens = tokenizeWifi(body)
        for (tok in tokens) {
            val colon = indexOfUnescaped(tok, ':')
            if (colon < 0) continue
            val key = tok.substring(0, colon).uppercase()
            val value = unescapeWifi(tok.substring(colon + 1))
            when (key) {
                "T" -> encryption = value
                "S" -> ssid = value
                "P" -> password = value
            }
        }
        if (ssid.isEmpty()) return null
        return WifiInfo(ssid = ssid, password = password, encryption = encryption)
    }

    /** Splits the body of a WIFI payload on unescaped `;` characters. */
    private fun tokenizeWifi(body: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var i = 0
        while (i < body.length) {
            val c = body[i]
            if (c == '\\' && i + 1 < body.length) {
                sb.append(c).append(body[i + 1]); i += 2; continue
            }
            if (c == ';') {
                if (sb.isNotEmpty()) out += sb.toString()
                sb.setLength(0)
                i++; continue
            }
            sb.append(c); i++
        }
        if (sb.isNotEmpty()) out += sb.toString()
        return out
    }

    /** Finds the first `:` that is not preceded by a backslash. */
    private fun indexOfUnescaped(s: String, target: Char): Int {
        var i = 0
        while (i < s.length) {
            if (s[i] == '\\' && i + 1 < s.length) { i += 2; continue }
            if (s[i] == target) return i
            i++
        }
        return -1
    }

    private fun unescapeWifi(value: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < value.length) {
            if (value[i] == '\\' && i + 1 < value.length) {
                sb.append(value[i + 1])
                i += 2
            } else {
                sb.append(value[i])
                i++
            }
        }
        return sb.toString()
    }

    // ---- vCard parser ----
    data class ContactInfo(val name: String, val phone: String, val email: String, val org: String)

    fun parseVCard(rawContent: String): ContactInfo {
        // RFC 6350 §3.2: long property values may be folded across lines by
        // prefixing the continuation line with a space or tab. Unfold first.
        val unfolded = unfoldVCardLines(rawContent)
        var name = ""
        var phone = ""
        var email = ""
        var org = ""
        for (line in unfolded) {
            val trimmed = line.trim()
            when {
                trimmed.uppercase().startsWith("FN:") -> name = trimmed.substring(3).trim()
                trimmed.uppercase().startsWith("TEL:") -> phone = trimmed.substring(4).trim()
                trimmed.uppercase().startsWith("TEL;") && phone.isEmpty() -> {
                    val idx = trimmed.indexOf(':')
                    if (idx >= 0) phone = trimmed.substring(idx + 1).trim()
                }
                trimmed.uppercase().startsWith("EMAIL:") -> email = trimmed.substring(6).trim()
                trimmed.uppercase().startsWith("EMAIL;") && email.isEmpty() -> {
                    val idx = trimmed.indexOf(':')
                    if (idx >= 0) email = trimmed.substring(idx + 1).trim()
                }
                trimmed.uppercase().startsWith("ORG:") -> org = trimmed.substring(4).trim()
            }
        }
        return ContactInfo(name, phone, email, org)
    }

    private fun unfoldVCardLines(rawContent: String): List<String> {
        val rawLines = rawContent.replace("\r\n", "\n").split("\n")
        val out = mutableListOf<String>()
        for (line in rawLines) {
            if (line.isNotEmpty() && (line[0] == ' ' || line[0] == '\t') && out.isNotEmpty()) {
                out[out.lastIndex] = out.last() + line.substring(1)
            } else {
                out += line
            }
        }
        return out
    }

    // ---- Calendar (vEvent) parser ----
    data class CalendarEvent(val summary: String, val start: String, val end: String, val location: String)

    fun parseCalendarEvent(rawContent: String): CalendarEvent {
        var summary = ""
        var start = ""
        var end = ""
        var location = ""
        for (line in rawContent.split("\n")) {
            val trimmed = line.trim()
            when {
                trimmed.uppercase().startsWith("SUMMARY:") -> summary = trimmed.substring(8).trim()
                trimmed.uppercase().startsWith("DTSTART") && !trimmed.uppercase().startsWith("DTSTART;") -> {
                    val idx = trimmed.indexOf(':')
                    if (idx >= 0) start = trimmed.substring(idx + 1).trim()
                }
                trimmed.uppercase().startsWith("DTSTART;") && start.isEmpty() -> {
                    val idx = trimmed.indexOf(':')
                    if (idx >= 0) start = trimmed.substring(idx + 1).trim()
                }
                trimmed.uppercase().startsWith("DTEND") && !trimmed.uppercase().startsWith("DTEND;") -> {
                    val idx = trimmed.indexOf(':')
                    if (idx >= 0) end = trimmed.substring(idx + 1).trim()
                }
                trimmed.uppercase().startsWith("DTEND;") && end.isEmpty() -> {
                    val idx = trimmed.indexOf(':')
                    if (idx >= 0) end = trimmed.substring(idx + 1).trim()
                }
                trimmed.uppercase().startsWith("LOCATION:") -> location = trimmed.substring(9).trim()
            }
        }
        return CalendarEvent(summary, start, end, location)
    }

    // ---- Geo parser ----
    data class GeoInfo(val latitude: Double, val longitude: Double)

    fun parseGeo(rawContent: String): GeoInfo? {
        // RFC 5870 allows integer or decimal lat/lon. Optional query/altitude
        // suffixes are ignored. e.g. geo:40,-73  /  geo:40.7,-73.9?q=...  /
        // geo:40.7,-73.9,100
        val regex = Regex("""geo:(-?\d+(?:\.\d+)?),(-?\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
        val match = regex.find(rawContent) ?: return null
        return GeoInfo(
            latitude = match.groupValues[1].toDouble(),
            longitude = match.groupValues[2].toDouble()
        )
    }
}
