package com.dhanuk.quickscanpro.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BarcodeTypeDetectorTest {

    @Test
    fun detectType_emptyString_returnsUnknown() {
        assertEquals(BarcodeTypeDetector.TYPE_UNKNOWN, BarcodeTypeDetector.detectType(""))
    }

    @Test
    fun detectType_blankString_returnsUnknown() {
        assertEquals(BarcodeTypeDetector.TYPE_UNKNOWN, BarcodeTypeDetector.detectType("   "))
    }

    @Test
    fun detectType_httpUrl_returnsUrl() {
        assertEquals(BarcodeTypeDetector.TYPE_URL, BarcodeTypeDetector.detectType("http://example.com"))
    }

    @Test
    fun detectType_httpsUrl_returnsUrl() {
        assertEquals(BarcodeTypeDetector.TYPE_URL, BarcodeTypeDetector.detectType("https://example.com"))
    }

    @Test
    fun detectType_uppercaseHttp_returnsUrl() {
        assertEquals(BarcodeTypeDetector.TYPE_URL, BarcodeTypeDetector.detectType("HTTPS://EXAMPLE.COM"))
    }

    @Test
    fun detectType_mailto_returnsEmail() {
        assertEquals(BarcodeTypeDetector.TYPE_EMAIL, BarcodeTypeDetector.detectType("mailto:user@example.com"))
    }

    @Test
    fun detectType_tel_returnsPhone() {
        assertEquals(BarcodeTypeDetector.TYPE_PHONE, BarcodeTypeDetector.detectType("tel:+1234567890"))
    }

    @Test
    fun detectType_sms_returnsSms() {
        assertEquals(BarcodeTypeDetector.TYPE_SMS, BarcodeTypeDetector.detectType("sms:+1234567890"))
    }

    @Test
    fun detectType_smsto_returnsSms() {
        assertEquals(BarcodeTypeDetector.TYPE_SMS, BarcodeTypeDetector.detectType("smsto:+1234567890"))
    }

    @Test
    fun detectType_wifi_returnsWifi() {
        assertEquals(BarcodeTypeDetector.TYPE_WIFI, BarcodeTypeDetector.detectType("WIFI:T:WPA;S:MyNet;P:pass123;;"))
    }

    @Test
    fun detectType_vcard_returnsVcard() {
        assertEquals(BarcodeTypeDetector.TYPE_VCARD, BarcodeTypeDetector.detectType("BEGIN:VCARD\nFN:John\nEND:VCARD"))
    }

    @Test
    fun detectType_vevent_returnsCalendar() {
        assertEquals(BarcodeTypeDetector.TYPE_CALENDAR, BarcodeTypeDetector.detectType("BEGIN:VEVENT\nSUMMARY:Test\nEND:VEVENT"))
    }

    @Test
    fun detectType_geo_returnsGeo() {
        assertEquals(BarcodeTypeDetector.TYPE_GEO, BarcodeTypeDetector.detectType("geo:37.7749,-122.4194"))
    }

    @Test
    fun detectType_plainEmail_returnsEmail() {
        assertEquals(BarcodeTypeDetector.TYPE_EMAIL, BarcodeTypeDetector.detectType("user@example.com"))
    }

    @Test
    fun detectType_phoneNumber_returnsPhone() {
        assertEquals(BarcodeTypeDetector.TYPE_PHONE, BarcodeTypeDetector.detectType("+1-234-567-890"))
    }

    @Test
    fun detectType_productBarcode_returnsProduct() {
        assertEquals(BarcodeTypeDetector.TYPE_PRODUCT, BarcodeTypeDetector.detectType("1234567890128"))
    }

    @Test
    fun detectType_ean8_returnsProduct() {
        assertEquals(BarcodeTypeDetector.TYPE_PRODUCT, BarcodeTypeDetector.detectType("12345670"))
    }

    @Test
    fun detectType_plainText_returnsText() {
        assertEquals(BarcodeTypeDetector.TYPE_TEXT, BarcodeTypeDetector.detectType("Hello World"))
    }

    // ---- WiFi parser ----

    @Test
    fun parseWifi_validInput_returnsInfo() {
        val wifi = BarcodeTypeDetector.parseWifi("WIFI:T:WPA;S:MyNetwork;P:MyPassword;;")
        assertNotNull(wifi)
        assertEquals("MyNetwork", wifi!!.ssid)
        assertEquals("MyPassword", wifi.password)
        assertEquals("WPA", wifi.encryption)
    }

    @Test
    fun parseWifi_caseInsensitive_returnsInfo() {
        val wifi = BarcodeTypeDetector.parseWifi("wifi:T:wpa;S:Net;P:pass;;")
        assertNotNull(wifi)
        assertEquals("Net", wifi!!.ssid)
    }

    @Test
    fun parseWifi_invalidInput_returnsNull() {
        assertNull(BarcodeTypeDetector.parseWifi("not a wifi code"))
    }

    @Test
    fun parseWifi_escapedChars_returnsUnescaped() {
        val wifi = BarcodeTypeDetector.parseWifi("""WIFI:T:WPA;S:My\;Net;P:pass\;word;;""")
        assertNotNull(wifi)
        assertEquals("My;Net", wifi!!.ssid)
        assertEquals("pass;word", wifi.password)
    }

    // ---- vCard parser ----

    @Test
    fun parseVCard_fullCard_returnsAllFields() {
        val vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:John Doe\nTEL:+1234567890\nEMAIL:john@example.com\nORG:Acme Corp\nEND:VCARD"
        val contact = BarcodeTypeDetector.parseVCard(vcard)
        assertEquals("John Doe", contact.name)
        assertEquals("+1234567890", contact.phone)
        assertEquals("john@example.com", contact.email)
        assertEquals("Acme Corp", contact.org)
    }

    @Test
    fun parseVCard_emptyCard_returnsEmptyFields() {
        val contact = BarcodeTypeDetector.parseVCard("BEGIN:VCARD\nEND:VCARD")
        assertEquals("", contact.name)
        assertEquals("", contact.phone)
        assertEquals("", contact.email)
    }

    @Test
    fun parseVCard_typedTel_parsesPhone() {
        val vcard = "BEGIN:VCARD\nTEL;TYPE=CELL:+1234567890\nEND:VCARD"
        val contact = BarcodeTypeDetector.parseVCard(vcard)
        assertEquals("+1234567890", contact.phone)
    }

    // ---- Geo parser ----

    @Test
    fun parseGeo_validInput_returnsInfo() {
        val geo = BarcodeTypeDetector.parseGeo("geo:37.7749,-122.4194")
        assertNotNull(geo)
        assertEquals(37.7749, geo!!.latitude, 0.0001)
        assertEquals(-122.4194, geo.longitude, 0.0001)
    }

    @Test
    fun parseGeo_negativeCoords_returnsInfo() {
        val geo = BarcodeTypeDetector.parseGeo("geo:-33.8688,151.2093")
        assertNotNull(geo)
        assertEquals(-33.8688, geo!!.latitude, 0.0001)
        assertEquals(151.2093, geo.longitude, 0.0001)
    }

    @Test
    fun parseGeo_invalidInput_returnsNull() {
        assertNull(BarcodeTypeDetector.parseGeo("not a geo code"))
    }

    // ---- Calendar parser ----

    @Test
    fun parseCalendarEvent_fullEvent_returnsAllFields() {
        val event = "BEGIN:VEVENT\nSUMMARY:Team Meeting\nDTSTART:20250101T100000Z\nDTEND:20250101T110000Z\nLOCATION:Office\nEND:VEVENT"
        val cal = BarcodeTypeDetector.parseCalendarEvent(event)
        assertEquals("Team Meeting", cal.summary)
        assertEquals("20250101T100000Z", cal.start)
        assertEquals("20250101T110000Z", cal.end)
        assertEquals("Office", cal.location)
    }
}
