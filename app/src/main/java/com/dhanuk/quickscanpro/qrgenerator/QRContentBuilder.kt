package com.dhanuk.quickscanpro.qrgenerator

object QRContentBuilder {

    fun buildWifi(ssid: String, password: String, encryption: String): String {
        val enc = when (encryption.uppercase()) {
            "WPA", "WPA2", "WPA3" -> "WPA"
            "WEP" -> "WEP"
            "NOPASS" -> "nopass"
            else -> "WPA"
        }
        return "WIFI:T:$enc;S:$ssid;P:$password;;"
    }

    fun buildVCARD(name: String, phone: String, email: String, org: String): String {
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("FN:$name")
            if (phone.isNotBlank()) appendLine("TEL:$phone")
            if (email.isNotBlank()) appendLine("EMAIL:$email")
            if (org.isNotBlank()) appendLine("ORG:$org")
            appendLine("END:VCARD")
        }
    }

    fun buildCalendar(summary: String, location: String, startDate: String, endDate: String): String {
        return buildString {
            appendLine("BEGIN:VEVENT")
            appendLine("SUMMARY:$summary")
            if (location.isNotBlank()) appendLine("LOCATION:$location")
            appendLine("DTSTART:$startDate")
            appendLine("DTEND:$endDate")
            appendLine("END:VEVENT")
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
            "smsto:$to:${android.net.Uri.encode(message)}"
        } else "smsto:$to"
    }

    fun buildPhone(number: String): String = "tel:$number"

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
