package com.wstxda.toolkit.manager.mediaoutput

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaRouter2
import android.os.Build
import android.provider.Settings

object AudioPanelDispatcher {

    fun triggerOutputSwitching(context: Context): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()

        val oemHandled = when {
            isXiaomiGroup(manufacturer) -> triggerXiaomi(context)
            isSamsung(manufacturer) -> triggerSamsung(context)
            else -> false
        }
        if (oemHandled) return true

        if (invokeNativeSystemRouter(context)) return true

        if (triggerGenericAndroid(context)) return true

        return openVolumePanelFallback(context)
    }

    private fun isXiaomiGroup(manufacturer: String): Boolean {
        return listOf("xiaomi", "poco", "redmi", "blackshark").any { manufacturer.contains(it) }
    }

    private fun isSamsung(manufacturer: String): Boolean {
        return manufacturer.contains("samsung")
    }

    private fun triggerXiaomi(context: Context): Boolean {
        val intents = arrayOf(
            Intent("miui.intent.action.ACTIVITY_MIPLAY_DETAIL"), Intent().setClassName(
                "miui.systemui.plugin", "miui.systemui.miplay.MiPlayDetailActivity"
            )
        )
        return intents.any { performIntentLaunch(context, it) }
    }

    private fun triggerSamsung(context: Context): Boolean {
        val intents = arrayOf(
            Intent().setClassName(
                "com.samsung.android.mdx.quickboard",
                "com.samsung.android.mdx.quickboard.view.MediaActivity"
            )
        )
        return intents.any { performIntentLaunch(context, it) }
    }

    private fun triggerGenericAndroid(context: Context): Boolean {
        val intents = arrayOf(
            Intent("com.android.systemui.action.LAUNCH_MEDIA_OUTPUT_DIALOG").setPackage("com.android.systemui"),
            Intent("com.android.settings.panel.action.MEDIA_OUTPUT")
        )

        return intents.any { performIntentLaunch(context, it) }
    }

    private fun openVolumePanelFallback(context: Context): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(Settings.Panel.ACTION_VOLUME)
            if (performIntentLaunch(context, intent)) return true
        }

        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_SAME,
                AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_PLAY_SOUND
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun performIntentLaunch(ctx: Context, intent: Intent): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun invokeNativeSystemRouter(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return try {
                MediaRouter2.getInstance(ctx).showSystemOutputSwitcher()
                true
            } catch (_: Exception) {
                false
            }
        }
        return false
    }
}