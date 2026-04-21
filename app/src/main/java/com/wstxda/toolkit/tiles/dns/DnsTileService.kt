package com.wstxda.toolkit.tiles.dns

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.WriteSecureSettingsActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.dns.DnsManager
import com.wstxda.toolkit.ui.icon.DnsIconProvider
import com.wstxda.toolkit.ui.label.DnsLabelProvider
import kotlinx.coroutines.flow.Flow

class DnsTileService : BaseTileService() {

    private val dnsManager by lazy { DnsManager(applicationContext) }
    private val labelProvider by lazy { DnsLabelProvider(applicationContext) }
    private val iconProvider by lazy { DnsIconProvider(applicationContext) }

    override fun onDestroy() {
        super.onDestroy()
        dnsManager.cleanup()
    }

    override fun onClick() {
        if (!dnsManager.hasPermission()) {
            startActivityAndCollapse(WriteSecureSettingsActivity::class.java)
            return
        }
        dnsManager.cycleProvider()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        dnsManager.currentProvider,
    )

    override fun updateTile() {
        val hasPermission = dnsManager.hasPermission()
        val currentProvider = dnsManager.getCurrentProviderInternal()
        val isDisabled = currentProvider == com.wstxda.toolkit.manager.dns.DnsProvider.DISABLED

        setTileState(
            state = if (hasPermission && !isDisabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(currentProvider, hasPermission),
            subtitle = labelProvider.getSubtitle(currentProvider, hasPermission),
            icon = iconProvider.getIcon(currentProvider, hasPermission),
        )
    }
}