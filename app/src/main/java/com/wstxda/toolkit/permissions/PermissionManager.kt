package com.wstxda.toolkit.permissions

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.getSystemService
import com.wstxda.toolkit.services.accessibility.TileAccessibilityService

class PermissionManager(context: Context) {

    private val appContext = context.applicationContext

    fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = ComponentName(appContext, TileAccessibilityService::class.java)

        val enabledServicesSetting = Settings.Secure.getString(
            appContext.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
    }

    fun hasWriteSettingsPermission(): Boolean = Settings.System.canWrite(appContext)

    fun hasWriteSecureSettingsPermission(): Boolean = try {
        val currentMode = Settings.Global.getString(appContext.contentResolver, "private_dns_mode")
        Settings.Global.putString(appContext.contentResolver, "private_dns_mode", currentMode)
        true
    } catch (_: SecurityException) {
        false
    }

    fun hasDoNotDisturbPermission(): Boolean {
        val notificationManager = appContext.getSystemService<NotificationManager>()
        return notificationManager?.isNotificationPolicyAccessGranted == true
    }
}