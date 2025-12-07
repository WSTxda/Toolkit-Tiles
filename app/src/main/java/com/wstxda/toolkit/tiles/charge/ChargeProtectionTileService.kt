package com.wstxda.toolkit.tiles.charge

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.AccessibilityPermissionActivity
import com.wstxda.toolkit.activity.NotificationPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.charge.ChargeProtectionModule
import com.wstxda.toolkit.manager.charge.ChargeState
import com.wstxda.toolkit.ui.icon.ChargeIconProvider
import com.wstxda.toolkit.ui.label.ChargeLabelProvider
import kotlinx.coroutines.flow.Flow

class ChargeProtectionTileService : BaseTileService() {

    private val chargeManager by lazy { ChargeProtectionModule.getInstance(applicationContext) }
    private val chargeLabelProvider by lazy { ChargeLabelProvider(applicationContext) }
    private val chargeIconProvider by lazy { ChargeIconProvider(applicationContext) }

    override fun onClick() {

        if (!chargeManager.hasPermission()) {
            startActivityAndCollapse(AccessibilityPermissionActivity::class.java)
            return
        }

        if (!chargeManager.hasNotificationPermission()) {
            startActivityAndCollapse(NotificationPermissionActivity::class.java)
            return
        }

        chargeManager.toggle()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(chargeManager.currentState)
    }

    override fun updateTile() {
        val state = chargeManager.currentState.value

        setTileState(
            state = if (state == ChargeState.INACTIVE) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE,
            label = chargeLabelProvider.getLabel(state),
            subtitle = chargeLabelProvider.getSubtitle(state),
            icon = chargeIconProvider.getIcon(state)
        )
    }
}