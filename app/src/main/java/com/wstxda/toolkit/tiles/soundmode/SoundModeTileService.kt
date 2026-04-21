package com.wstxda.toolkit.tiles.soundmode

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.NotificationPolicyPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.soundmode.SoundModeManager
import com.wstxda.toolkit.ui.icon.SoundModeIconProvider
import com.wstxda.toolkit.ui.label.SoundModeLabelProvider
import kotlinx.coroutines.flow.Flow

class SoundModeTileService : BaseTileService() {

    private val soundModeManager by lazy { SoundModeManager(applicationContext) }
    private val labelProvider by lazy { SoundModeLabelProvider(applicationContext) }
    private val iconProvider by lazy { SoundModeIconProvider(applicationContext) }

    override fun onDestroy() {
        super.onDestroy()
        soundModeManager.cleanup()
    }

    override fun onClick() {
        if (!soundModeManager.hasPermission()) {
            startActivityAndCollapse(NotificationPolicyPermissionActivity::class.java)
            return
        }
        soundModeManager.cycleMode()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        soundModeManager.currentMode,
    )

    override fun updateTile() {
        val hasPermission = soundModeManager.hasPermission()
        val currentMode = soundModeManager.currentMode.value

        setTileState(
            state = if (hasPermission) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(currentMode, hasPermission),
            subtitle = labelProvider.getSubtitle(hasPermission),
            icon = iconProvider.getIcon(currentMode, hasPermission),
        )
    }
}