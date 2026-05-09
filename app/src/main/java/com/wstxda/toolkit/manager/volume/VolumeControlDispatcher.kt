package com.wstxda.toolkit.manager.volume

import android.content.Context
import android.media.AudioManager

object VolumeControlDispatcher {

    fun openVolumeControl(context: Context): Boolean {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI
            )
            true
        } catch (_: Exception) {
            false
        }
    }
}