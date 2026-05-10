package com.wstxda.toolkit.tiles.musicsearch

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.MusicSearchActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.ui.icon.MusicSearchIconProvider
import com.wstxda.toolkit.ui.label.MusicSearchLabelProvider

class MusicSearchTileService : BaseTileService() {

    private val labelProvider by lazy { MusicSearchLabelProvider(applicationContext) }
    private val iconProvider by lazy { MusicSearchIconProvider(applicationContext) }

    override fun onClick() {
        startActivityAndCollapse(MusicSearchActivity::class.java)
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