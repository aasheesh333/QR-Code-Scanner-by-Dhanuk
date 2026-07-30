package com.dhanuk.quickscanpro.util

import com.dhanuk.quickscanpro.util.BarcodeTypeDetector.Companion.TYPE_TEXT
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector.Companion.TYPE_URL
import java.util.Locale

/**
 * Detects whether scanned text is non-English / foreign enough to warrant a
 * "Translate" suggestion. Uses a cheap character-block heuristic.
 *
 * Free, fully offline, no API needed. Suggests the user open Google Translate
 * via an Intent rather than bundling heavy offline language packs.
 */
object TextLanguageDetector {

    private val latinRanges = listOf(
        // Basic Latin + Latin-1 Supplement + Latin Extended A/B (English/European)
        0x0020..0x024F,
        // Cyrillic is "foreign" to English users but the user might still want translate
    )

    fun isLikelyForeign(text: String): Boolean {
        if (text.length < 4) return false
        var nonLatin = 0
        var total = 0
        for (c in text) {
            if (c.isWhitespace()) continue
            total++
            val cp = c.code
            val inLatin = latinRanges.any { cp in it }
            if (!inLatin) nonLatin++
        }
        if (total == 0) return false
        val ratio = nonLatin.toFloat() / total
        return ratio > 0.25f
    }

    fun sourceLanguageHint(text: String): String {
        var hasCJK = false
        var hasArabic = false
        var hasDevanagari = false
        var hasCyrillic = false
        for (c in text) {
            val cp = c.code
            when {
                cp in 0x4E00..0x9FFF || cp in 0x3040..0x30FF -> hasCJK = true
                cp in 0x0600..0x06FF -> hasArabic = true
                cp in 0x0900..0x097F -> hasDevanagari = true
                cp in 0x0400..0x04FF -> hasCyrillic = true
            }
        }
        return when {
            hasCJK -> "Chinese/Japanese"
            hasArabic -> "Arabic"
            hasDevanagari -> "Hindi/Devanagari"
            hasCyrillic -> "Russian/Cyrillic"
            else -> Locale.getDefault().displayLanguage
        }
    }
}
