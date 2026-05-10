package com.wstxda.toolkit.tiles.clipboard

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.ClipboardActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.ui.icon.ClipboardIconProvider
import com.wstxda.toolkit.ui.label.ClipboardLabelProvider

class ClipboardTileService : BaseTileService() {

    private val labelProvider by lazy { ClipboardLabelProvider(applicationContext) }
    private val iconProvider by lazy { ClipboardIconProvider(applicationContext) }

    override fun onClick() {
        startActivityAndCollapse(ClipboardActivity::class.java)
    }

    override fun updateTile() {
        setTileState(
            state = Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(),
            subtitle = labelProvider.getSubtitle(),
            icon = iconProvider.getIcon(),
        )
    }
}