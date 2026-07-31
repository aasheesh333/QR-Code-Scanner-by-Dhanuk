package com.dhanuk.quickscanpro.qrgenerator

object QRContentBuilder {

    fun buildWifi(ssid: String, password: String, encryption: String): String {
        val enc = when (encryption.uppercase()) {
            "WPA", "WPA2", "WPA3" -> "WPA"
            "WEP" -> "WEP"
            "NOPASS" -> "nopass"
            else -> "WPA"
        }
        return "WIFI:T:$enc;S:${escapeWifi(ssid)};P:${escapeWifi(password)};;"
    }

    private fun escapeWifi(value: String): String {
        val sb = StringBuilder()
        for (c in value) {
            if (c == '\\' || c == ';' || c == ',' || c == ':' || c == '"') sb.append('\\')
            sb.append(c)
        }
        return sb.toString()
    }

    fun buildVCARD(name: String, phone: String, email: String, org: String): String {
        val escName = escapeVCard(name)
        val parts = escName.split(" ", limit = 2)
        val familyName = parts.getOrNull(0) ?: ""
        val givenName = parts.getOrNull(1) ?: ""
        return buildString {
            append("BEGIN:VCARD\r\n")
            append("VERSION:3.0\r\n")
            append("N:$familyName;$givenName;;;\r\n")
            append("FN:$escName\r\n")
            if (phone.isNotBlank()) append("TEL;TYPE=CELL:${escapeVCard(phone)}\r\n")
            if (email.isNotBlank()) append("EMAIL;TYPE=INTERNET:${escapeVCard(email)}\r\n")
            if (org.isNotBlank()) append("ORG:${escapeVCard(org)}\r\n")
            append("END:VCARD\r\n")
        }
    }

    private fun escapeVCard(value: String): String {
        return value.replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace(";", "\\;")
            .replace("\n", "\\n")
    }

    fun buildCalendar(summary: String, location: String, startDate: String, endDate: String): String {
        return buildString {
            append("BEGIN:VCALENDAR\r\n")
            append("VERSION:2.0\r\n")
            append("PRODID:-//QuickScan Pro//EN\r\n")
            append("BEGIN:VEVENT\r\n")
            append("UID:${System.currentTimeMillis()}@quickscanpro\r\n")
            append("DTSTAMP:${formatStampNow()}\r\n")
            append("SUMMARY:${escapeICal(summary)}\r\n")
            if (location.isNotBlank()) append("LOCATION:${escapeICal(location)}\r\n")
            append("DTSTART:${escapeICal(startDate)}\r\n")
            append("DTEND:${escapeICal(endDate)}\r\n")
            append("END:VEVENT\r\n")
            append("END:VCALENDAR\r\n")
        }
    }

    private fun escapeICal(value: String): String {
        return value.replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
    }

    private fun formatStampNow(): String {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        val yyyy = cal.get(java.util.Calendar.YEAR)
        val mm = String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1)
        val dd = String.format("%02d", cal.get(java.util.Calendar.DAY_OF_MONTH))
        val hh = String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY))
        val mi = String.format("%02d", cal.get(java.util.Calendar.MINUTE))
        val ss = String.format("%02d", cal.get(java.util.Calendar.SECOND))
        return "${yyyy}${mm}${dd}T${hh}${mi}${ss}Z"
    }

    fun buildUrl(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    fun buildEmail(to: String, subject: String, body: String): String {
        val sb = StringBuilder("mailto:$to")
        val params = mutableListOf<String>()
        if (subject.isNotBlank()) params.add("subject=${android.net.Uri.encode(subject)}")
        if (body.isNotBlank()) params.add("body=${android.net.Uri.encode(body)}")
        if (params.isNotEmpty()) sb.append("?").append(params.joinToString("&"))
        return sb.toString()
    }

    fun buildSMS(to: String, message: String): String {
        return if (message.isNotBlank()) {
            "sms:$to?body=${android.net.Uri.encode(message)}"
        } else "sms:$to"
    }

    fun buildPhone(number: String): String {
        val normalized = number.replace(Regex("[^+0-9]"), "")
        return "tel:$normalized"
    }

    fun buildGeo(latitude: Double, longitude: Double): String = "geo:$latitude,$longitude"

    enum class QRType(val displayName: String) {
        TEXT("Plain Text"),
        URL("URL / Website"),
        WIFI("WiFi Network"),
        VCARD("Contact Card"),
        EMAIL("Email"),
        SMS("Text Message"),
        PHONE("Phone Number"),
        CALENDAR("Calendar Event")
    }
}
