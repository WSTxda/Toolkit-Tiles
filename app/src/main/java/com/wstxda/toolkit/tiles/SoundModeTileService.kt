package com.wstxda.toolkit.tiles

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.wstxda.toolkit.R
import com.wstxda.toolkit.activity.NotificationPolicyPermissionActivity
import com.wstxda.toolkit.services.update

class SoundModeTileService : TileService() {
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(<strong>Context</strong>.AUDIO_SERVICE) as AudioManager
        notificationManager = getSystemService(<strong>Context</strong>.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(this, NotificationPolicyPermissionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this, 0, intent, PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION") startActivityAndCollapse(intent)
            }
            return
        }

        when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }
            AudioManager.RINGER_MODE_VIBRATE -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            }
            AudioManager.RINGER_MODE_SILENT -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        }
        updateTileState()
    }

    private fun updateTileState() {
        qsTile?.update {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
                state = Tile.STATE_INACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    subtitle = getString(R.string.tile_label_setup)
                }
                return@update
            }

            when (audioManager.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> {
                    state = Tile.STATE_ACTIVE
                    icon = Icon.createWithResource(this@SoundModeTileService, R.drawable.ic_volume_up)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        subtitle = getString(R.string.sound_mode_normal)
                    }
                }
                AudioManager.RINGER_MODE_VIBRATE -> {
                    state = Tile.STATE_ACTIVE
                    icon = Icon.createWithResource(this@SoundModeTileService, R.drawable.ic_vibrate)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        subtitle = getString(R.string.sound_mode_vibrate)
                    }
                }
                AudioManager.RINGER_MODE_SILENT -> {
                    state = Tile.STATE_INACTIVE
                    icon = Icon.createWithResource(this@SoundModeTileService, R.drawable.ic_volume_off)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        subtitle = getString(R.string.sound_mode_silent)
                    }
                }
            }
        }
    }
}
