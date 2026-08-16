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
        assertEquals(2, report.breachCount)
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
    fun check_multipleSignals_doesNotMarkAsLeaked() {
        // Heuristics surface signals but never flip `leaked` to true.
        // `leaked` is reserved for confirmed breach-list matches.
        val report = PasswordLeakChecker.check("a.b.c.d.suspicious-site.xyz")
        assertFalse(report.leaked)
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

    @Test
    fun parseRangeResponse_findsSuffixCount() {
        val body = "0018A45C4D1DEF81644B54AB7F969B88D65:1\n03310E67579D88A1D3E0E50E69C5D0B2A:4"
        assertEquals(4L, PasswordLeakChecker.parseRangeResponse(body, "03310E67579D88A1D3E0E50E69C5D0B2A"))
        assertEquals(1L, PasswordLeakChecker.parseRangeResponse(body, "0018A45C4D1DEF81644B54AB7F969B88D65"))
    }

    @Test
    fun parseRangeResponse_missingSuffix_returnsZero() {
        assertEquals(0L, PasswordLeakChecker.parseRangeResponse("AAA:5", "BBB"))
    }

    @Test
    fun sha1Hex_passwordIsLowercaseHex40() {
        val hash = PasswordLeakChecker.sha1Hex("password")
        assertEquals(40, hash.length)
        assertTrue(hash.all { it in "0123456789abcdef" })
    }

    @Test
    fun sha1Hex_knownVector() {
        assertEquals("5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8", PasswordLeakChecker.sha1Hex("password"))
    }

    @Test
    fun check_facebook_returnsLeaked() {
        val report = PasswordLeakChecker.check("facebook.com")
        assertTrue(report.leaked)
        assertEquals("facebook.com", report.domain)
    }

    @Test
    fun check_zomato_returnsLeaked() {
        assertTrue(PasswordLeakChecker.check("zomato.com").leaked)
    }

    @Test
    fun check_noTld_fallsBackToDotCom() {
        val report = PasswordLeakChecker.check("shein")
        assertTrue(report.leaked)
    }

    @Test
    fun check_instagram_notFlaggedAsBreached() {
        val report = PasswordLeakChecker.check("instagram.com")
        assertFalse(report.leaked)
    }
}