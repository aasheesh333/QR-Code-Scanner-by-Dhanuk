package com.dhanuk.quickscanpro.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordToolsTest {

    @Test
    fun emptyPasswordIsVeryWeak() {
        val analysis = PasswordTools.analyze("")
        assertEquals(PasswordTools.Strength.VERY_WEAK, analysis.strength)
        assertEquals(0, analysis.entropyBits)
    }

    @Test
    fun commonPasswordIsFlaggedWeak() {
        val analysis = PasswordTools.analyze("password")
        assertTrue(analysis.strength == PasswordTools.Strength.VERY_WEAK ||
            analysis.strength == PasswordTools.Strength.WEAK)
        assertTrue(analysis.signals.any { it.contains("common", ignoreCase = true) })
    }

    @Test
    fun shortPasswordFlagged() {
        val analysis = PasswordTools.analyze("Ab1!")
        assertTrue(analysis.signals.any { it.contains("short", ignoreCase = true) })
    }

    @Test
    fun sequenceDetected() {
        val analysis = PasswordTools.analyze("Qwerty123!xxZz")
        assertTrue(analysis.signals.any { it.contains("sequence", ignoreCase = true) })
    }

    @Test
    fun repeatedCharsDetected() {
        val analysis = PasswordTools.analyze("Aaaa111!Zz")
        assertTrue(analysis.signals.any { it.contains("repeated", ignoreCase = true) })
    }

    @Test
    fun strongPasswordScoresHigh() {
        val analysis = PasswordTools.analyze("Kj8#mQ2%vP9!xR4z")
        assertTrue(
            "expected high strength, got ${analysis.strength}",
            analysis.strength == PasswordTools.Strength.STRONG ||
                analysis.strength == PasswordTools.Strength.EXCELLENT
        )
        assertTrue(analysis.entropyBits >= 80)
    }

    @Test
    fun generatedPasswordHasRequiredLength() {
        val pw = PasswordTools.generate(16)
        assertEquals(16, pw.length)
    }

    @Test
    fun generatedPasswordRespectsBounds() {
        assertEquals(8, PasswordTools.generate(4).length)
        assertEquals(64, PasswordTools.generate(200).length)
    }

    @Test
    fun generatedPasswordContainsGuaranteedClasses() {
        repeat(20) {
            val pw = PasswordTools.generate(16, includeSymbols = true)
            assertTrue(pw.any { it in 'a'..'z' })
            assertTrue(pw.any { it in 'A'..'Z' })
            assertTrue(pw.any { it.isDigit() })
            assertTrue(pw.any { !it.isLetterOrDigit() })
        }
    }

    @Test
    fun generatedPasswordsAreRandom() {
        val set = (1..25).map { PasswordTools.generate(16) }.toSet()
        assertTrue(set.size > 20)
    }

    @Test
    fun excludeSymbolsProducesNoSymbols() {
        repeat(10) {
            val pw = PasswordTools.generate(16, includeSymbols = false)
            assertFalse(pw.any { !it.isLetterOrDigit() })
        }
    }

    @Test
    fun entropyIncreasesWithLength() {
        val short = PasswordTools.analyze("Ab1!Ab1!")
        val long = PasswordTools.analyze("Ab1!Ab1!Ab1!Ab1!")
        assertTrue(long.entropyBits > short.entropyBits)
    }
}
