package com.wstxda.toolkit.tiles.screenshot

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.AccessibilityPermissionActivity
import com.wstxda.toolkit.activity.ScreenshotActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.screenshot.ScreenshotModule
import com.wstxda.toolkit.ui.icon.ScreenshotIconProvider
import com.wstxda.toolkit.ui.label.ScreenshotLabelProvider
import kotlinx.coroutines.flow.Flow

class ScreenshotTileService : BaseTileService() {

    private val screenshotManager by lazy { ScreenshotModule.getInstance(applicationContext) }
    private val labelProvider by lazy { ScreenshotLabelProvider(applicationContext) }
    private val iconProvider by lazy { ScreenshotIconProvider(applicationContext) }

    override fun onClick() {
        if (screenshotManager.isPermissionGranted.value) {
            startActivityAndCollapse(ScreenshotActivity::class.java)
        } else {
            startActivityAndCollapse(AccessibilityPermissionActivity::class.java)
        }
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        screenshotManager.isPermissionGranted,
    )

    override fun updateTile() {
        val hasPermission = screenshotManager.isPermissionGranted.value

        setTileState(
            state = Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(),
            subtitle = labelProvider.getSubtitle(hasPermission),
            icon = iconProvider.getIcon(),
        )
    }
}