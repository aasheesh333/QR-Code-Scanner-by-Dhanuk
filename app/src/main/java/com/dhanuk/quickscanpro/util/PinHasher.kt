package com.dhanuk.quickscanpro.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted, slow-hashed vault PIN storage. The plaintext PIN is never persisted —
 * only "v1:iterations:saltBase64:hashBase64". Uses PBKDF2WithHmacSHA1, which is
 * available on every supported API level (minSdk 23).
 *
 * [verify] also accepts a legacy plaintext value so that PINs stored by older
 * builds keep working until the user next changes them.
 */
object PinHasher {

    private const val ITERATIONS = 20_000
    private const val KEY_LENGTH_BITS = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA1"

    fun hash(pin: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(pin.toCharArray(), salt, ITERATIONS)
        return "v1:$ITERATIONS:${salt.b64()}:${hash.b64()}"
    }

    fun verify(pin: String, stored: String): Boolean {
        if (stored.isBlank()) return false
        if (!stored.startsWith("v1:")) {
            // Legacy plaintext PIN from an earlier build.
            return constantTimeEquals(pin.toByteArray(), stored.toByteArray())
        }
        return try {
            val parts = stored.split(":")
            val iterations = parts[1].toInt()
            val salt = parts[2].deB64()
            val expected = parts[3].deB64()
            val actual = pbkdf2(pin.toCharArray(), salt, iterations)
            constantTimeEquals(actual, expected)
        } catch (_: Exception) {
            false
        }
    }

    private fun pbkdf2(pin: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin, salt, iterations, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) result = result or (a[i].toInt() xor b[i].toInt())
        return result == 0
    }

    private fun ByteArray.b64(): String = Base64.encodeToString(this, Base64.NO_WRAP)
    private fun String.deB64(): ByteArray = Base64.decode(this, Base64.NO_WRAP)
}
