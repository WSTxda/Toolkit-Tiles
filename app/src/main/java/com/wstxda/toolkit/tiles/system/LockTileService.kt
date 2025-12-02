package com.wstxda.toolkit.tiles.system

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.AccessibilityPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.system.LockManager
import com.wstxda.toolkit.ui.icon.LockIconProvider
import com.wstxda.toolkit.ui.label.LockLabelProvider
import kotlinx.coroutines.flow.Flow

class LockTileService : BaseTileService() {

    private val lockManager by lazy { LockManager(applicationContext) }
    private val lockLabelProvider by lazy { LockLabelProvider(applicationContext) }
    private val lockIconProvider by lazy { LockIconProvider(applicationContext) }

    override fun onDestroy() {
        super.onDestroy()
        lockManager.cleanup()
    }

    override fun onClick() {
        if (lockManager.isPermissionGranted.value) {
            lockManager.lockScreen()
        } else {
            startActivityAndCollapse(AccessibilityPermissionActivity::class.java)
        }
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(lockManager.isPermissionGranted)
    }

    override fun updateTile() {
        val hasPermission = lockManager.isPermissionGranted.value

        setTileState(
            state = Tile.STATE_INACTIVE,
            label = lockLabelProvider.getLabel(),
            subtitle = lockLabelProvider.getSubtitle(hasPermission),
            icon = lockIconProvider.getIcon()
        )
    }
}