package com.wstxda.toolkit.activity

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.wstxda.toolkit.tiles.battery.BatteryTileService
import com.wstxda.toolkit.tiles.breathing.BreathingTileService
import com.wstxda.toolkit.tiles.brightness.AutoBrightnessTileService
import com.wstxda.toolkit.tiles.caffeine.CaffeineTileService
import com.wstxda.toolkit.tiles.clipboard.ClipboardTileService
import com.wstxda.toolkit.tiles.dns.DnsTileService
import com.wstxda.toolkit.tiles.lock.LockTileService
import com.wstxda.toolkit.tiles.mediaoutput.MediaOutputTileService
import com.wstxda.toolkit.tiles.memory.MemoryTileService
import com.wstxda.toolkit.tiles.musicsearch.MusicSearchTileService
import com.wstxda.toolkit.tiles.networktraffic.NetworkTrafficTileService
import com.wstxda.toolkit.tiles.nfc.NfcTileService
import com.wstxda.toolkit.tiles.power.PowerTileService
import com.wstxda.toolkit.tiles.rotation.RotationTileService
import com.wstxda.toolkit.tiles.screenshot.ScreenshotTileService
import com.wstxda.toolkit.tiles.sos.SosTileService
import com.wstxda.toolkit.tiles.soundmode.SoundModeTileService
import com.wstxda.toolkit.tiles.temperature.TemperatureTileService
import com.wstxda.toolkit.tiles.usbdebugging.UsbDebuggingTileService
import com.wstxda.toolkit.tiles.volume.VolumeTileService
import androidx.core.net.toUri

class TilePreferencesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = resolveComponent()
        val intent = resolveIntent(component) ?: Intent(this, AboutAppActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            startActivity(
                Intent(this, AboutAppActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        finish()
    }

    private fun resolveComponent(): ComponentName? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_COMPONENT_NAME, ComponentName::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_COMPONENT_NAME)
        }
    }

    private fun resolveIntent(component: ComponentName?): Intent? {
        val className = component?.className ?: return null

        return when (className) {

            NetworkTrafficTileService::class.qualifiedName ->
                Intent(Settings.ACTION_WIFI_SETTINGS)

            BatteryTileService::class.qualifiedName,
            TemperatureTileService::class.qualifiedName ->
                Intent(Intent.ACTION_POWER_USAGE_SUMMARY)

            MemoryTileService::class.qualifiedName ->
                Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)

            ClipboardTileService::class.qualifiedName ->
                Intent(Settings.ACTION_PRIVACY_SETTINGS)

            MusicSearchTileService::class.qualifiedName ->
                Intent(Intent.ACTION_VIEW, "https://myactivity.google.com/myactivity?product=17".toUri())

            VolumeTileService::class.qualifiedName ->
                Intent(this, MediaOutputActivity::class.java)

            UsbDebuggingTileService::class.qualifiedName ->
                Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)

            AutoBrightnessTileService::class.qualifiedName,
            CaffeineTileService::class.qualifiedName,
            RotationTileService::class.qualifiedName ->
                Intent(Settings.ACTION_DISPLAY_SETTINGS)

            DnsTileService::class.qualifiedName ->
                Intent(Settings.ACTION_WIRELESS_SETTINGS)

            MediaOutputTileService::class.qualifiedName ->
                Intent(this, VolumeActivity::class.java)

            SoundModeTileService::class.qualifiedName ->
                Intent(Settings.ACTION_SOUND_SETTINGS)

            NfcTileService::class.qualifiedName ->
                Intent(Settings.ACTION_NFC_SETTINGS)

            LockTileService::class.qualifiedName,
            PowerTileService::class.qualifiedName,
            SosTileService::class.qualifiedName ->
                Intent(Settings.ACTION_SECURITY_SETTINGS)

            ScreenshotTileService::class.qualifiedName ->
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

            else -> null
        }
    }
}