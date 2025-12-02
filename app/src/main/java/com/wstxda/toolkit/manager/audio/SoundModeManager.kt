package com.wstxda.toolkit.manager.audio

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SoundModeManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val currentMode: StateFlow<SoundMode> = callbackFlow {
        trySend(getCurrentModeInternal())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
                    trySend(getCurrentModeInternal())
                }
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Context.RECEIVER_NOT_EXPORTED
            } else 0
        )

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: IllegalArgumentException) {
            }
        }
    }.distinctUntilChanged().stateIn(
        scope = managerScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = getCurrentModeInternal()
    )

    fun hasPermission(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun cycleMode() {
        if (!hasPermission()) return

        managerScope.launch {
            val current = getCurrentModeInternal()
            val newMode = when (current) {
                SoundMode.NORMAL -> SoundMode.VIBRATE
                SoundMode.VIBRATE -> SoundMode.SILENT
                SoundMode.SILENT -> SoundMode.NORMAL
            }
            audioManager.ringerMode = newMode.ringerMode
        }
    }

    fun cleanup() {
        managerScope.cancel()
    }

    fun getCurrentModeInternal(): SoundMode {
        return when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_VIBRATE -> SoundMode.VIBRATE
            AudioManager.RINGER_MODE_SILENT -> SoundMode.SILENT
            else -> SoundMode.NORMAL
        }
    }
}