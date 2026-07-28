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

        // Phone pattern
        if (content.matches(Regex("^\\+?[0-9\\-\\s()]{7,20}$"))) return TYPE_PHONE

        // Product barcode (EAN/UPC): 8, 12, 13, or 14 digits
        if (content.matches(Regex("^\\d{8,14}$"))) return TYPE_PRODUCT

        return TYPE_TEXT
    }

    // ---- WiFi parser ----
    data class WifiInfo(val ssid: String, val password: String, val encryption: String)

    fun parseWifi(rawContent: String): WifiInfo? {
        // WIFI:T:WPA;S:<ssid>;P:<password>;;
        val regex = Regex("""WIFI:T:(\w+);S:(.*?);P:(.*?);+""", RegexOption.IGNORE_CASE)
        val match = regex.find(rawContent) ?: return null
        return WifiInfo(
            ssid = match.groupValues[2],
            password = match.groupValues[3],
            encryption = match.groupValues[1]
        )
    }

    // ---- vCard parser ----
    data class ContactInfo(val name: String, val phone: String, val email: String, val org: String)

    fun parseVCard(rawContent: String): ContactInfo {
        var name = ""
        var phone = ""
        var email = ""
        var org = ""
        for (line in rawContent.split("\n")) {
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
        val regex = Regex("""geo:(-?\d+\.\d+),(-?\d+\.\d+)""", RegexOption.IGNORE_CASE)
        val match = regex.find(rawContent) ?: return null
        return GeoInfo(
            latitude = match.groupValues[1].toDouble(),
            longitude = match.groupValues[2].toDouble()
        )
    }
}
