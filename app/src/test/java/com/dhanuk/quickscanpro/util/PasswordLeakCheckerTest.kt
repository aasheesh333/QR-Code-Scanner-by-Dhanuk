package com.dhanuk.quickscanpro.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordLeakCheckerTest {

    @Test
    fun check_knownLeakedDomain_linkedin_returnsLeaked() {
        val report = PasswordLeakChecker.check("linkedin.com")
        assertTrue(report.leaked)
        assertEquals(1, report.breachCount)
        assertEquals(2012, report.firstSeenYear)
    }

    @Test
    fun check_knownLeakedDomain_adobe_returnsLeaked() {
        val report = PasswordLeakChecker.check("adobe.com")
        assertTrue(report.leaked)
        assertEquals(2013, report.firstSeenYear)
    }

    @Test
    fun check_knownLeakedDomain_yahoo_returnsMultipleBreaches() {
        val report = PasswordLeakChecker.check("yahoo.com")
        assertTrue(report.leaked)
        assertEquals(2, report.breachCount)
    }

    @Test
    fun check_wwwPrefix_strippedAndLeaked() {
        val report = PasswordLeakChecker.check("www.linkedin.com")
        assertTrue(report.leaked)
        assertEquals("linkedin.com", report.domain)
    }

    @Test
    fun check_httpsPrefix_strippedAndLeaked() {
        val report = PasswordLeakChecker.check("https://linkedin.com")
        assertTrue(report.leaked)
        assertEquals("linkedin.com", report.domain)
    }

    @Test
    fun check_safeDomain_returnsNotLeaked() {
        val report = PasswordLeakChecker.check("my-safe-site.org")
        assertFalse(report.leaked)
        assertEquals(0, report.breachCount)
    }

    @Test
    fun check_blankInput_returnsNotLeaked() {
        val report = PasswordLeakChecker.check("")
        assertFalse(report.leaked)
    }

    @Test
    fun check_highAbuseTld_addsSignal() {
        val report = PasswordLeakChecker.check("suspicious-site.xyz")
        assertTrue(report.signals.any { it.contains("High-abuse TLD") })
    }

    @Test
    fun check_longDomain_addsSignal() {
        val report = PasswordLeakChecker.check("this-is-a-very-long-suspicious-domain-name.com")
        assertTrue(report.signals.any { it.contains("Unusually long domain") })
    }

    @Test
    fun check_deeplyNestedSubdomain_addsSignal() {
        val report = PasswordLeakChecker.check("a.b.c.d.example.com")
        assertTrue(report.signals.any { it.contains("Deeply nested subdomain") })
    }

    @Test
    fun check_typosquat_addsSignal() {
        val report = PasswordLeakChecker.check("gogle.com")
        assertTrue(report.signals.any { it.contains("typosquat") })
    }

    @Test
    fun check_multipleSignals_marksAsLeaked() {
        val report = PasswordLeakChecker.check("a.b.c.d.suspicious-site.xyz")
        assertTrue(report.leaked)
        assertTrue(report.signals.size >= 2)
    }

    @Test
    fun check_domainLowercased() {
        val report = PasswordLeakChecker.check("LINKEDIN.COM")
        assertTrue(report.leaked)
        assertEquals("linkedin.com", report.domain)
    }

    @Test
    fun sha1Prefix_returnsCorrectLength() {
        val prefix = PasswordLeakChecker.sha1Prefix("test", 5)
        assertEquals(5, prefix.length)
    }

    @Test
    fun sha1Prefix_uppercase() {
        val prefix = PasswordLeakChecker.sha1Prefix("test", 8)
        assertEquals(prefix, prefix.uppercase())
    }

    @Test
    fun sha1Prefix_sameInput_returnsSameHash() {
        val p1 = PasswordLeakChecker.sha1Prefix("hello", 10)
        val p2 = PasswordLeakChecker.sha1Prefix("hello", 10)
        assertEquals(p1, p2)
    }

    @Test
    fun sha1Prefix_differentInputs_returnDifferentHashes() {
        val p1 = PasswordLeakChecker.sha1Prefix("hello", 10)
        val p2 = PasswordLeakChecker.sha1Prefix("world", 10)
        assert(p1 != p2)
    }
}
