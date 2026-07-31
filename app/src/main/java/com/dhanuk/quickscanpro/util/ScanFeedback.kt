package com.dhanuk.quickscanpro.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Context

object ScanFeedback {

    private var toneGenerator: ToneGenerator? = null

    @Synchronized
    fun playBeep(durationMs: Int = 150) {
        try {
            val tg = toneGenerator ?: ToneGenerator(
                AudioManager.STREAM_MUSIC,
                80
            ).also { toneGenerator = it }
            tg.startTone(ToneGenerator.TONE_PROP_BEEP, durationMs)
        } catch (_: Exception) {
        }
    }

    fun vibrate(context: Context, durationMs: Long = 40) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(VibratorManager::class.java)
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Vibrator::class.java)
                v?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {
        }
    }
}
