package com.dhanuk.quickscanpro.qrgenerator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QRContentBuilderTest {

    @Test
    fun buildWifi_wpa_returnsCorrectFormat() {
        val result = QRContentBuilder.buildWifi("MyNet", "pass123", "WPA")
        assertEquals("WIFI:T:WPA;S:MyNet;P:pass123;;", result)
    }

    @Test
    fun buildWifi_wpa2_normalizesToWpa() {
        val result = QRContentBuilder.buildWifi("Net", "pass", "WPA2")
        assertEquals("WIFI:T:WPA;S:Net;P:pass;;", result)
    }

    @Test
    fun buildWifi_wpa3_normalizesToWpa() {
        val result = QRContentBuilder.buildWifi("Net", "pass", "WPA3")
        assertEquals("WIFI:T:WPA;S:Net;P:pass;;", result)
    }

    @Test
    fun buildWifi_wep_returnsWep() {
        val result = QRContentBuilder.buildWifi("Net", "pass", "WEP")
        assertEquals("WIFI:T:WEP;S:Net;P:pass;;", result)
    }

    @Test
    fun buildWifi_nopass_returnsNopass() {
        val result = QRContentBuilder.buildWifi("Net", "", "NOPASS")
        assertEquals("WIFI:T:nopass;S:Net;P:;;", result)
    }

    @Test
    fun buildWifi_unknownEncryption_defaultsToWpa() {
        val result = QRContentBuilder.buildWifi("Net", "pass", "UNKNOWN")
        assertEquals("WIFI:T:WPA;S:Net;P:pass;;", result)
    }

    @Test
    fun buildWifi_specialChars_escaped() {
        val result = QRContentBuilder.buildWifi("My;Net", "pass:word", "WPA")
        assertTrue(result.contains("S:My\\;Net;"))
        assertTrue(result.contains("P:pass\\:word;"))
    }

    @Test
    fun buildVCard_fullCard_returnsCorrectFormat() {
        val result = QRContentBuilder.buildVCARD("John Doe", "+1234567890", "john@example.com", "Acme")
        assertTrue(result.contains("BEGIN:VCARD"))
        assertTrue(result.contains("VERSION:3.0"))
        assertTrue(result.contains("FN:John Doe"))
        assertTrue(result.contains("TEL;TYPE=CELL:+1234567890"))
        assertTrue(result.contains("EMAIL;TYPE=INTERNET:john@example.com"))
        assertTrue(result.contains("ORG:Acme"))
        assertTrue(result.contains("END:VCARD"))
    }

    @Test
    fun buildVCard_emptyFields_omitted() {
        val result = QRContentBuilder.buildVCARD("John", "", "", "")
        assertTrue(result.contains("FN:John"))
        assertTrue(!result.contains("TEL:"))
        assertTrue(!result.contains("EMAIL:"))
        assertTrue(!result.contains("ORG:"))
    }

    @Test
    fun buildCalendar_fullEvent_returnsCorrectFormat() {
        val result = QRContentBuilder.buildCalendar("Meeting", "Office", "20250101T100000Z", "20250101T110000Z")
        assertTrue(result.contains("BEGIN:VEVENT"))
        assertTrue(result.contains("SUMMARY:Meeting"))
        assertTrue(result.contains("LOCATION:Office"))
        assertTrue(result.contains("DTSTART:20250101T100000Z"))
        assertTrue(result.contains("DTEND:20250101T110000Z"))
        assertTrue(result.contains("END:VEVENT"))
    }

    @Test
    fun buildCalendar_emptyLocation_omitted() {
        val result = QRContentBuilder.buildCalendar("Meeting", "", "start", "end")
        assertTrue(!result.contains("LOCATION:"))
    }

    @Test
    fun buildPhone_returnsTelFormat() {
        assertEquals("tel:+1234567890", QRContentBuilder.buildPhone("+1234567890"))
    }

    @Test
    fun buildGeo_returnsGeoFormat() {
        val result = QRContentBuilder.buildGeo(37.7749, -122.4194)
        assertEquals("geo:37.7749,-122.4194", result)
    }

    @Test
    fun buildGeo_negativeCoords_returnsGeoFormat() {
        val result = QRContentBuilder.buildGeo(-33.8688, 151.2093)
        assertEquals("geo:-33.8688,151.2093", result)
    }

    @Test
    fun qrType_displayName_correct() {
        assertEquals("Plain Text", QRContentBuilder.QRType.TEXT.displayName)
        assertEquals("URL / Website", QRContentBuilder.QRType.URL.displayName)
        assertEquals("WiFi Network", QRContentBuilder.QRType.WIFI.displayName)
        assertEquals("Contact Card", QRContentBuilder.QRType.VCARD.displayName)
    }
}
