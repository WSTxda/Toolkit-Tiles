package com.wstxda.toolkit.tiles.lock

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.AccessibilityPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.lock.LockModule
import com.wstxda.toolkit.ui.icon.LockIconProvider
import com.wstxda.toolkit.ui.label.LockLabelProvider
import kotlinx.coroutines.flow.Flow

class LockTileService : BaseTileService() {

    private val lockManager by lazy { LockModule.getInstance(applicationContext) }
    private val labelProvider by lazy { LockLabelProvider(applicationContext) }
    private val iconProvider by lazy { LockIconProvider(applicationContext) }

    override fun onClick() {
        if (!lockManager.isPermissionGranted.value) {
            startActivityAndCollapse(AccessibilityPermissionActivity::class.java)
            return
        }
        lockManager.lockScreen()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        lockManager.isPermissionGranted,
    )

    override fun updateTile() {
        val hasPermission = lockManager.isPermissionGranted.value

        setTileState(
            state = Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(),
            subtitle = labelProvider.getSubtitle(hasPermission),
            icon = iconProvider.getIcon(),
        )
    }
}