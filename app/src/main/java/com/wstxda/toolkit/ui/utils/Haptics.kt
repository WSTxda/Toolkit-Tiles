package com.wstxda.toolkit.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class Haptics(context: Context) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(VibratorManager::class.java)
        vibratorManager?.defaultVibrator ?: context.getSystemService(Vibrator::class.java)!!
    } else {
        context.getSystemService(Vibrator::class.java)!!
    }

    @SuppressLint("InlinedApi")
    fun tick() = performSafely(
        effectId = VibrationEffect.EFFECT_TICK, fallbackDuration = 10L, fallbackAmplitude = 100
    )

    @SuppressLint("InlinedApi")
    fun click() = performSafely(
        effectId = VibrationEffect.EFFECT_CLICK,
        fallbackDuration = 25L,
        fallbackAmplitude = VibrationEffect.DEFAULT_AMPLITUDE
    )

    fun long(duration: Long, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        if (!hasVibrator()) return
        vibrateCompat(VibrationEffect.createOneShot(duration, amplitude))
    }

    fun cancel() {
        if (hasVibrator()) vibrator.cancel()
    }

    private fun hasVibrator(): Boolean = vibrator.hasVibrator()

    private fun performSafely(effectId: Int, fallbackDuration: Long, fallbackAmplitude: Int) {
        if (!hasVibrator()) return

        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(effectId)
        } else {
            VibrationEffect.createOneShot(fallbackDuration, fallbackAmplitude)
        }
        vibrateCompat(effect)
    }

    private fun vibrateCompat(effect: VibrationEffect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val attributes =
                VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_HARDWARE_FEEDBACK)
                    .build()
            vibrator.vibrate(effect, attributes)
        } else {
            val attributes =
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).build()
            @Suppress("DEPRECATION") vibrator.vibrate(effect, attributes)
        }
    }
}