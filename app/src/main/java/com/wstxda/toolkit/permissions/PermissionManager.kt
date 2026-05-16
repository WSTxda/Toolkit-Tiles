package com.wstxda.toolkit.permissions

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.getSystemService
import com.wstxda.toolkit.services.accessibility.TileAccessibilityService

class PermissionManager(context: Context) {

    private val appContext = context.applicationContext

    fun isAccessibilityServiceEnabled(): Boolean {
        val am = appContext.getSystemService<android.view.accessibility.AccessibilityManager>()
        val enabledServices = am?.getEnabledAccessibilityServiceList(
            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        val toolkitComponent = ComponentName(appContext, TileAccessibilityService::class.java).flattenToString()
        return enabledServices?.any {
            it.id.contains(toolkitComponent, ignoreCase = true) || it.resolveInfo.serviceInfo.packageName == appContext.packageName
        } == true
    }

    fun hasWriteSettingsPermission(): Boolean = Settings.System.canWrite(appContext)

    fun hasWriteSecureSettingsPermission(): Boolean =
        appContext.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED

    fun hasDoNotDisturbPermission(): Boolean {
        val notificationManager = appContext.getSystemService<NotificationManager>()
        return notificationManager?.isNotificationPolicyAccessGranted == true
    }
}