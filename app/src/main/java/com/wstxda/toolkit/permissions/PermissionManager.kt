package com.wstxda.toolkit.permissions

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.ContextCompat
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

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = appContext.getSystemService<LocationManager>() ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }
    }
}