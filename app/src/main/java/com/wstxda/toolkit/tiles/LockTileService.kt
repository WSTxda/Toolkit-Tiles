package com.wstxda.toolkit.tiles

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import com.wstxda.toolkit.R
import com.wstxda.toolkit.activity.AccessibilityPermissionActivity
import com.wstxda.toolkit.services.accessibility.TileAccessibilityAction
import com.wstxda.toolkit.services.accessibility.TileAccessibilityService
import com.wstxda.toolkit.services.update

private const val TAG = "LockTileService"

class LockTileService : TileService() {
    override fun onCreate() {
        Log.i(TAG, "Create")
        super.onCreate()
    }

    override fun onStartListening() {
        Log.i(TAG, "Start listening")
        super.onStartListening()
        updateTileState()
    }

    override fun onTileAdded() {
        Log.i(TAG, "Tile added")
        super.onTileAdded()
        updateTileState()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        Log.i(TAG, "Click")
        if (!TileAccessibilityService.isServiceEnabled(this)) {
            val intent = Intent(this, AccessibilityPermissionActivity::class.java).apply {
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

        val intent = Intent(this, TileAccessibilityService::class.java).apply {
            putExtra(
                TileAccessibilityService.ACTION_KEY, TileAccessibilityAction.LOCK_SCREEN
            )
        }
        startService(intent)
    }

    private fun updateTileState() {
        qsTile?.update {
            if (TileAccessibilityService.isServiceEnabled(this@LockTileService)) {
                state = Tile.STATE_ACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    subtitle = getString(R.string.tile_label_on)
                }
            } else {
                state = Tile.STATE_INACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    subtitle = getString(R.string.tile_label_setup)
                }
            }
        }
    }
}