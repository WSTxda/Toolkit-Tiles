package com.wstxda.toolkit.services.foreground

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.TileService
import androidx.core.app.NotificationCompat
import com.wstxda.toolkit.R

internal const val TOOLKIT_CHANNEL_ID = "TOOLKIT_CHANNEL"

fun Context.toolkitChannel() = NotificationChannel(
    TOOLKIT_CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW
)

fun TileService.channel() = toolkitChannel()
fun TileService.notification(): Notification = toolkitNotification()

fun Context.toolkitNotification(): Notification =
    NotificationCompat.Builder(this, TOOLKIT_CHANNEL_ID).setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(getString(R.string.notification_title))
        .setContentText(getString(R.string.notification_label))
        .setContentIntent(toolkitNotificationClickIntent()).build()

fun Context.toolkitNotificationClickIntent(): PendingIntent {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}