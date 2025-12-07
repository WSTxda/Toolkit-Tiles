package com.wstxda.toolkit.manager.charge

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.wstxda.toolkit.R
import com.wstxda.toolkit.ui.utils.Haptics
import kotlinx.coroutines.*
import androidx.core.net.toUri

class ChargeAlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "charge_protection_channel"
        const val ACTION_STOP = "com.wstxda.toolkit.ACTION_STOP_ALARM"
    }

    private var mediaPlayer: MediaPlayer? = null
    private val haptics by lazy { Haptics(this) }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private var originalVolume = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startAlarm()
        return START_STICKY
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun startAlarm() {
        createNotificationChannel()

        val stopIntent = Intent(this, ChargeAlarmService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.app_name)).setSmallIcon(R.drawable.ic_level_dot_zero)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM).setOngoing(true)
            .setContentIntent(pendingIntent).build()

        startForeground(199, notification)

        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            )
            setDataSource(
                applicationContext, "android.resource://$packageName/${R.raw.alarm}".toUri()
            )
            isLooping = true
            prepare()
            start()
        }

        serviceScope.launch {
            while (isActive) {
                haptics.long(1000, force = true)
                delay(1500)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
        haptics.cancel()
        serviceScope.cancel()

        ChargeProtectionModule.getInstance(this).resetState()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
            enableVibration(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}