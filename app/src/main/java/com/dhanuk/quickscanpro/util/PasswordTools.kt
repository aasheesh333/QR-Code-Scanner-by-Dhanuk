package com.dhanuk.quickscanpro.util

import java.security.SecureRandom

/**
 * Offline password strength analysis + secure generator.
 * Pure Kotlin — fully unit-testable, no Android APIs.
 */
object PasswordTools {

    enum class Strength(val label: String, val score: Int) {
        VERY_WEAK("Very weak", 20),
        WEAK("Weak", 40),
        FAIR("Fair", 60),
        STRONG("Strong", 80),
        EXCELLENT("Excellent", 100)
    }

    data class Analysis(
        val strength: Strength,
        val entropyBits: Int,
        val signals: List<String>
    )

    fun analyze(password: String): Analysis {
        val signals = mutableListOf<String>()
        if (password.isEmpty()) return Analysis(Strength.VERY_WEAK, 0, listOf("Empty password"))

        var pool = 0
        if (password.any { it in 'a'..'z' }) pool += 26
        if (password.any { it in 'A'..'Z' }) pool += 26
        if (password.any { it.isDigit() }) pool += 10
        if (password.any { !it.isLetterOrDigit() }) pool += 33

        val entropy = (password.length * log2(pool.coerceAtLeast(2))).toInt()

        if (password.length < 8) signals.add("Too short — use at least 12 characters")
        if (!password.any { it.isDigit() }) signals.add("Add numbers")
        if (!password.any { it.isUpperCase() }) signals.add("Add uppercase letters")
        if (!password.any { !it.isLetterOrDigit() }) signals.add("Add symbols")

        val repeated = Regex("(.)\\1{2,}").containsMatchIn(password)
        if (repeated) signals.add("Avoid repeated characters (aaa, 111)")

        val sequential = containsSequence(password)
        if (sequential) signals.add("Avoid sequences (abc, 123, qwerty)")

        if (COMMON_PASSWORDS.contains(password.lowercase())) {
            signals.add("This is one of the most common passwords")
        }

        val strength = when {
            entropy < 28 -> Strength.VERY_WEAK
            entropy < 40 -> Strength.WEAK
            entropy < 60 -> Strength.FAIR
            entropy < 80 -> Strength.STRONG
            else -> if (commonOrPatterned(password)) Strength.WEAK else Strength.EXCELLENT
        }

        return Analysis(strength, entropy, signals)
    }

    fun generate(length: Int = 16, includeSymbols: Boolean = true): String {
        val len = length.coerceIn(8, 64)
        val lower = "abcdefghijkmnopqrstuvwxyz"
        val upper = "ABCDEFGHJKLMNPQRSTUVWXYZ"
        val digits = "23456789"
        val symbols = "!@#\$%^&*_-+=?"
        val pool = lower + upper + digits + (if (includeSymbols) symbols else "")
        val random = SecureRandom()

        // Guarantee at least one of each guaranteed class
        val guaranteed = buildList {
            add(lower[random.nextInt(lower.length)])
            add(upper[random.nextInt(upper.length)])
            add(digits[random.nextInt(digits.length)])
            if (includeSymbols) add(symbols[random.nextInt(symbols.length)])
        }
        val rest = List(len - guaranteed.size) { pool[random.nextInt(pool.length)] }
        return (guaranteed + rest).shuffled(random).joinToString("")
    }

    private fun log2(n: Int): Double = Math.log(n.toDouble()) / Math.log(2.0)

    private fun containsSequence(password: String): Boolean {
        val lower = password.lowercase()
        val keys = listOf("abcdefghijklmnop", "zyxwvutsrqponmlkjih", "0123456789", "qwertyuiop", "asdfghjkl", "zxcvbnm")
        for (key in keys) {
            for (i in 0..key.length - 4) {
                val fwd = key.substring(i, i + 4)
                val rev = fwd.reversed()
                if (lower.contains(fwd) || lower.contains(rev)) return true
            }
        }
        return false
    }

    private fun commonOrPatterned(password: String): Boolean {
        val lower = password.lowercase()
        return COMMON_PASSWORDS.contains(lower) || lower.toSet().size <= 2
    }

    private val COMMON_PASSWORDS = setOf(
        "password", "123456", "12345678", "qwerty", "abc123", "password1",
        "iloveyou", "admin", "welcome", "monkey", "dragon", "letmein",
        "111111", "sunshine", "princess", "football", "baseball", "shadow"
    )
}
