package com.wstxda.toolkit.tiles.nfc

import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.activity.WriteSecureSettingsActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.nfc.NfcModule
import com.wstxda.toolkit.ui.icon.NfcIconProvider
import com.wstxda.toolkit.ui.label.NfcLabelProvider
import kotlinx.coroutines.flow.Flow

class NfcTileService : BaseTileService() {

    private val nfcManager by lazy { NfcModule.getInstance(applicationContext) }
    private val labelProvider by lazy { NfcLabelProvider(applicationContext) }
    private val iconProvider by lazy { NfcIconProvider(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        nfcManager.start()
    }

    override fun onStopListening() {
        super.onStopListening()
        nfcManager.stop()
    }

    override fun onClick() {
        if (!nfcManager.hasHardware) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show()
            return
        }
        if (!nfcManager.hasPermission()) {
            startActivityAndCollapse(WriteSecureSettingsActivity::class.java)
            return
        }
        nfcManager.toggle()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        nfcManager.isEnabled,
    )

    override fun updateTile() {
        val hasHardware = nfcManager.hasHardware
        val hasPermission = nfcManager.hasPermission()
        val isEnabled = nfcManager.isEnabled.value

        setTileState(
            state = when {
                !hasHardware -> Tile.STATE_UNAVAILABLE
                isEnabled && hasPermission -> Tile.STATE_ACTIVE
                else -> Tile.STATE_INACTIVE
            },
            label = labelProvider.getLabel(),
            subtitle = labelProvider.getSubtitle(isEnabled, hasPermission, hasHardware),
            icon = iconProvider.getIcon(isEnabled),
        )
    }
}