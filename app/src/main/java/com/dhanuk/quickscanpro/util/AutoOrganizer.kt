package com.dhanuk.quickscanpro.util

import com.dhanuk.quickscanpro.util.BarcodeTypeDetector.TYPE_URL
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector.TYPE_WIFI
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector.TYPE_VCARD
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector.TYPE_PHONE
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector.TYPE_EMAIL
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector.TYPE_SMS
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector.TYPE_CALENDAR
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector.TYPE_TEXT
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector.TYPE_PRODUCT
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector.TYPE_GEO

/**
 * Auto-categorizes scans into smart folders based on detected barcode type.
 * Free, offline, deterministic — no AI API needed.
 */
object AutoOrganizer {

    const val CAT_LINKS = "Links"
    const val CAT_WIFI = "WiFi"
    const val CAT_CONTACTS = "Contacts"
    const val CAT_PAYMENTS = "Payments"
    const val CAT_EVENTS = "Events"
    const val CAT_PRODUCTS = "Products"
    const val CAT_PLACES = "Places"
    const val CAT_NOTES = "Notes"

    fun categorize(type: String, content: String): String {
        val lowered = content.lowercase()
        return when (type) {
            TYPE_URL -> {
                when {
                    lowered.contains("upi://") || lowered.contains("paytm://") ||
                    lowered.contains("phonepe://") || lowered.contains("bharatqr") -> CAT_PAYMENTS
                    else -> CAT_LINKS
                }
            }
            TYPE_WIFI -> CAT_WIFI
            TYPE_VCARD -> CAT_CONTACTS
            TYPE_PHONE -> CAT_CONTACTS
            TYPE_EMAIL -> CAT_CONTACTS
            TYPE_SMS -> CAT_CONTACTS
            TYPE_CALENDAR -> CAT_EVENTS
            TYPE_PRODUCT -> CAT_PRODUCTS
            TYPE_GEO -> CAT_PLACES
            TYPE_TEXT -> {
                when {
                    lowered.startsWith("upi://") || lowered.contains("bhim") -> CAT_PAYMENTS
                    lowered.length > 100 -> CAT_NOTES
                    else -> CAT_NOTES
                }
            }
            else -> CAT_NOTES
        }
    }

    val allCategories = listOf(
        CAT_LINKS, CAT_WIFI, CAT_CONTACTS, CAT_PAYMENTS, CAT_EVENTS, CAT_PRODUCTS, CAT_PLACES, CAT_NOTES
    )

    fun emojiFor(category: String): String = when (category) {
        CAT_LINKS -> "🔗"
        CAT_WIFI -> "📶"
        CAT_CONTACTS -> "👤"
        CAT_PAYMENTS -> "💳"
        CAT_EVENTS -> "📅"
        CAT_PRODUCTS -> "📦"
        CAT_PLACES -> "📍"
        CAT_NOTES -> "📝"
        else -> "📁"
    }
}
