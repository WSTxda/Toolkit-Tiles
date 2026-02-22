package com.wstxda.toolkit.ui.utils

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class Haptics(private val context: Context) {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(VibratorManager::class.java)
            vibratorManager?.defaultVibrator ?: @Suppress("DEPRECATION") context.getSystemService(
                Vibrator::class.java
            )!!
        } else {
            @Suppress("DEPRECATION") context.getSystemService(Vibrator::class.java)!!
        }
    }

    fun tick() {
        perform(
            effectId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK else -1,
            fallbackDuration = 10L,
            fallbackAmplitude = 100
        )
    }

    fun click() {
        perform(
            effectId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_CLICK else -1,
            fallbackDuration = 25L,
            fallbackAmplitude = VibrationEffect.DEFAULT_AMPLITUDE
        )
    }

    fun long(
        duration: Long, force: Boolean = false, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE
    ) {
        performOneShot(duration, amplitude, force)
    }

    fun cancel() {
        vibrator.cancel()
    }

    private fun perform(effectId: Int, fallbackDuration: Long, fallbackAmplitude: Int) {
        if (!isAllowed(force = false)) return

        val effect: VibrationEffect =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && effectId != -1) {
                VibrationEffect.createPredefined(effectId)
            } else {
                VibrationEffect.createOneShot(fallbackDuration, fallbackAmplitude)
            }

        vibrateCompat(effect, force = false)
    }

    private fun performOneShot(duration: Long, amplitude: Int, force: Boolean) {
        if (!isAllowed(force)) return
        val effect = VibrationEffect.createOneShot(duration, amplitude)
        vibrateCompat(effect, force)
    }

    private fun vibrateCompat(effect: VibrationEffect, force: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            val usage =
                if (force) VibrationAttributes.USAGE_ALARM else VibrationAttributes.USAGE_TOUCH
            val attrs = VibrationAttributes.Builder().setUsage(usage).build()
            vibrator.vibrate(effect, attrs)
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(effect)
        }
    }

    private fun isAllowed(force: Boolean): Boolean {
        if (!vibrator.hasVibrator()) return false
        if (force) return true

        val am = context.getSystemService(AudioManager::class.java)
        if (am.ringerMode == AudioManager.RINGER_MODE_SILENT) return false

        val nm = context.getSystemService(NotificationManager::class.java)
        return try {
            val filter = nm.currentInterruptionFilter
            filter <= NotificationManager.INTERRUPTION_FILTER_PRIORITY
        } catch (_: Exception) {
            true
        }
    }
}