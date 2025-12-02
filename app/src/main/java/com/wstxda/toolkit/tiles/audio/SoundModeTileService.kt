package com.wstxda.toolkit.tiles.audio

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.NotificationPolicyPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.audio.SoundModeManager
import com.wstxda.toolkit.ui.icon.SoundModeIconProvider
import com.wstxda.toolkit.ui.label.SoundModeLabelProvider
import kotlinx.coroutines.flow.Flow

class SoundModeTileService : BaseTileService() {

    private val soundModeManager by lazy { SoundModeManager(applicationContext) }
    private val soundModeLabelProvider by lazy { SoundModeLabelProvider(applicationContext) }
    private val soundModeIconProvider by lazy { SoundModeIconProvider(applicationContext) }

    override fun onDestroy() {
        super.onDestroy()
        soundModeManager.cleanup()
    }

    override fun onClick() {
        if (soundModeManager.hasPermission()) {
            soundModeManager.cycleMode()
        } else {
            startActivityAndCollapse(NotificationPolicyPermissionActivity::class.java)
        }
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(soundModeManager.currentMode)
    }

    override fun updateTile() {
        val hasPermission = soundModeManager.hasPermission()
        val mode = soundModeManager.currentMode.value
        setTileState(
            state = if (hasPermission) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = soundModeLabelProvider.getLabel(mode, hasPermission),
            subtitle = soundModeLabelProvider.getSubtitle(hasPermission),
            icon = soundModeIconProvider.getIcon(mode, hasPermission)
        )
    }
}