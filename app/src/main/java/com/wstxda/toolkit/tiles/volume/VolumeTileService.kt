package com.wstxda.toolkit.tiles.volume

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.VolumeActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.ui.icon.VolumeIconProvider
import com.wstxda.toolkit.ui.label.VolumeLabelProvider

class VolumeTileService : BaseTileService() {

    private val labelProvider by lazy { VolumeLabelProvider(applicationContext) }
    private val iconProvider by lazy { VolumeIconProvider(applicationContext) }

    override fun onClick() {
        startActivityAndCollapse(VolumeActivity::class.java)
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