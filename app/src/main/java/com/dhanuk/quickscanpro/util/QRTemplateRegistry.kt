package com.dhanuk.quickscanpro.util

import com.dhanuk.quickscanpro.qrgenerator.QRContentBuilder

/**
 * Built-in QR template gallery — pre-made 1-tap starters for the 4 most
 * common QR types. Pure local, offline.
 */
object QRTemplateRegistry {

    data class Template(
        val key: String,
        val title: String,
        val subtitle: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val type: QRContentBuilder.QRType,
        val prefill: TemplatePrefill
    )

    data class TemplatePrefill(
        val f1: String = "",
        val f2: String = "",
        val f3: String = "",
        val f4: String = ""
    )

    val templates: List<Template> = listOf(
        Template(
            key = "business_card",
            title = "Business Card",
            subtitle = "vCard with name, phone, email",
            icon = androidx.compose.material.icons.Icons.Filled.Badge,
            type = QRContentBuilder.QRType.VCARD,
            prefill = TemplatePrefill(f1 = "", f2 = "", f3 = "")
        ),
        Template(
            key = "home_wifi",
            title = "Home Wi-Fi",
            subtitle = "Share your Wi-Fi with guests",
            icon = androidx.compose.material.icons.Icons.Filled.Wifi,
            type = QRContentBuilder.QRType.WIFI,
            prefill = TemplatePrefill(f1 = "", f2 = "", f3 = "WPA")
        ),
        Template(
            key = "event_invite",
            title = "Event Invite",
            subtitle = "ICS calendar event",
            icon = androidx.compose.material.icons.Icons.Filled.Event,
            type = QRContentBuilder.QRType.CALENDAR,
            prefill = TemplatePrefill(f1 = "", f2 = "", f3 = "", f4 = "")
        ),
        Template(
            key = "contact_phone",
            title = "Tap-to-Call",
            subtitle = "tel: link",
            icon = androidx.compose.material.icons.Icons.Filled.Phone,
            type = QRContentBuilder.QRType.PHONE,
            prefill = TemplatePrefill(f1 = "")
        ),
        Template(
            key = "simple_link",
            title = "Website Link",
            subtitle = "Email or chat-app URL",
            icon = androidx.compose.material.icons.Icons.Filled.Link,
            type = QRContentBuilder.QRType.URL,
            prefill = TemplatePrefill(f1 = "https://")
        ),
        Template(
            key = "support_email",
            title = "Support Email",
            subtitle = "Pre-filled mail message",
            icon = androidx.compose.material.icons.Icons.Filled.Email,
            type = QRContentBuilder.QRType.EMAIL,
            prefill = TemplatePrefill(f1 = "", f2 = "", f3 = "")
        )
    )
}
