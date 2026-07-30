package com.dhanuk.quickscanpro.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Lightweight singleton for speak-aloud (TTS) feature.
 * Free, on-device via Android's built-in TextToSpeech engine.
 */
object VoiceSpeaker {
    private const val TAG = "VoiceSpeaker"
    private var tts: TextToSpeech? = null
    private var initialised = false

    fun init(context: Context) {
        if (initialised || tts != null) return
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.getDefault()
                    initialised = true
                    Log.d(TAG, "TTS engine ready")
                } else {
                    Log.w(TAG, "TTS init failed: $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "TTS init exception", e)
        }
    }

    fun speak(text: String) {
        if (!initialised || tts == null) return
        try {
            tts?.stop()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "qr_scan_result")
        } catch (e: Exception) {
            Log.e(TAG, "TTS speak failed", e)
        }
    }

    fun stop() {
        try { tts?.stop() } catch (_: Exception) {}
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        initialised = false
    }
}
