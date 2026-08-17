package com.dhanuk.quickscanpro.util

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Lightweight singleton for speak-aloud (TTS) feature.
 * Free, on-device via Android's built-in TextToSpeech engine.
 *
 * The TTS engine initialises asynchronously; [speak] called before the
 * engine is ready is queued and spoken once init completes, so the first
 * tap is heard almost instantly after the engine warms up.
 */
object VoiceSpeaker {
    private const val TAG = "VoiceSpeaker"
    private var tts: TextToSpeech? = null
    private var initialised = false
    private var pendingText: String? = null
    private var contextRef: Context? = null

    /** Safe to call repeatedly — only the first call initialises. Warm it up early (e.g. app start). */
    fun init(context: Context) {
        if (initialised || tts != null) {
            contextRef = context.applicationContext
            return
        }
        try {
            contextRef = context.applicationContext
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.getDefault()
                    initialised = true
                    Log.d(TAG, "TTS engine ready")
                    val pending = pendingText
                    if (pending != null) {
                        pendingText = null
                        speak(pending)
                    }
                } else {
                    initialised = false
                    tts = null
                    Log.w(TAG, "TTS init failed: $status")
                }
            }
        } catch (e: Exception) {
            tts = null
            initialised = false
            Log.e(TAG, "TTS init exception", e)
        }
    }

    /**
     * Speaks the text. If the engine is not ready yet the text is queued
     * and spoken as soon as the engine becomes available, instead of the
     * first tap silently doing nothing.
     */
    fun speak(text: String) {
        if (!initialised) {
            pendingText = text
            val ctx = contextRef ?: return
            init(ctx)
            return
        }
        try {
            val available = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "qr_scan_result")
            if (available == TextToSpeech.LANG_MISSING_DATA || available == TextToSpeech.LANG_NOT_SUPPORTED) {
                val ctx = contextRef ?: return
                runCatching {
                    ctx.startActivity(
                        Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=com.google.android.tts"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                Log.w(TAG, "TTS voice data missing for ${Locale.getDefault()}")
            }
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
        pendingText = null
    }
}
