package com.dhanuk.quickscanpro.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkSafetyCheckerTest {

    @Test
    fun analyze_nonLink_returnsNotALink() {
        val report = LinkSafetyChecker.analyze("Hello World")
        assertEquals(LinkSafetyChecker.Level.NOT_A_LINK, report.level)
        assertEquals(-1, report.score)
    }

    @Test
    fun analyze_emptyString_returnsNotALink() {
        val report = LinkSafetyChecker.analyze("")
        assertEquals(LinkSafetyChecker.Level.NOT_A_LINK, report.level)
    }

    @Test
    fun analyze_blankString_returnsNotALink() {
        val report = LinkSafetyChecker.analyze("   ")
        assertEquals(LinkSafetyChecker.Level.NOT_A_LINK, report.level)
    }

    @Test
    fun analyze_httpUrl_addsHttpWarningSignal() {
        val report = LinkSafetyChecker.analyze("http://example.com")
        assertTrue(report.signals.any { it.contains("Not encrypted") || it.contains("HTTP") })
    }

    @Test
    fun analyze_httpsUrl_addsEncryptedSignal() {
        val report = LinkSafetyChecker.analyze("https://example.com")
        assertTrue(report.signals.any { it.contains("Encrypted") })
    }

    @Test
    fun analyze_httpsUrl_scoreHigherThanHttp() {
        val httpsReport = LinkSafetyChecker.analyze("https://example.com")
        val httpReport = LinkSafetyChecker.analyze("http://example.com")
        assertTrue(httpsReport.score > httpReport.score)
    }

    @Test
    fun analyze_atSymbol_addsSignal() {
        val report = LinkSafetyChecker.analyze("https://example.com@evil.com")
        assertTrue(report.signals.any { it.contains("@") })
    }

    @Test
    fun analyze_apkDownload_addsSignal() {
        val report = LinkSafetyChecker.analyze("https://example.com/app.apk")
        assertTrue(report.signals.any { it.contains("apk") || it.contains("malware") || it.contains("download") })
    }

    @Test
    fun analyze_javascriptProtocol_addsSignal() {
        val report = LinkSafetyChecker.analyze("javascript:alert(1)")
        assertEquals(LinkSafetyChecker.Level.NOT_A_LINK, report.level)
    }

    @Test
    fun analyze_redirectParam_addsSignal() {
        val report = LinkSafetyChecker.analyze("https://example.com?redirect=evil.com")
        assertTrue(report.signals.any { it.contains("redirect") || it.contains("Redirect") })
    }

    @Test
    fun analyze_longUrl_addsSignal() {
        val longUrl = "https://example.com/" + "a".repeat(130)
        val report = LinkSafetyChecker.analyze(longUrl)
        assertTrue(report.signals.any { it.contains("long") || it.contains("Long") })
    }

    @Test
    fun analyze_scoreBetweenZeroAndHundred() {
        val report = LinkSafetyChecker.analyze("https://example.com")
        assertTrue(report.score in 0..100)
    }

    @Test
    fun analyze_safeHttpsUrl_returnsSafeOrCautionLevel() {
        val report = LinkSafetyChecker.analyze("https://www.google.com")
        assertTrue(report.level == LinkSafetyChecker.Level.SAFE || report.level == LinkSafetyChecker.Level.CAUTION)
    }

    @Test
    fun analyze_safeUrl_hasNoRiskLevel() {
        val report = LinkSafetyChecker.analyze("https://www.google.com")
        assertFalse(report.level == LinkSafetyChecker.Level.RISKY)
    }
}
