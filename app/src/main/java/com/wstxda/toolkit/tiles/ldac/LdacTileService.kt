package com.wstxda.toolkit.tiles.ldac

import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.activity.SecureSettingsPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.ldac.LdacModule
import com.wstxda.toolkit.ui.icon.LdacIconProvider
import com.wstxda.toolkit.ui.label.LdacLabelProvider
import com.wstxda.toolkit.ui.utils.Haptics
import kotlinx.coroutines.flow.Flow

class LdacTileService : BaseTileService() {

    private val ldacModule by lazy { LdacModule.getInstance(applicationContext) }
    private val ldacLabelProvider by lazy { LdacLabelProvider(applicationContext) }
    private val ldacIconProvider by lazy { LdacIconProvider(applicationContext) }
    private val haptics by lazy { Haptics(applicationContext) }

    override fun onStartListening() {
        ldacModule.startMonitoring()
        super.onStartListening()
    }

    override fun onStopListening() {
        super.onStopListening()
        ldacModule.stopMonitoring()
    }

    override fun onClick() {
        haptics.click()
        
        if (!ldacModule.isPermissionGranted()) {
            startActivityAndCollapse(SecureSettingsPermissionActivity::class.java)
            return
        }

        if (!ldacModule.isConnected.value) {
            Toast.makeText(this, R.string.ldac_not_connected, Toast.LENGTH_SHORT).show()
            return
        }

        ldacModule.cycleState()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(
            ldacModule.currentState,
            ldacModule.isConnected
        )
    }

    override fun updateTile() {
        val state = ldacModule.currentState.value
        val hasPermission = ldacModule.isPermissionGranted()
        val isConnected = ldacModule.isConnected.value

        val tileState = when {
            !hasPermission -> Tile.STATE_INACTIVE
            !isConnected -> Tile.STATE_INACTIVE
            else -> Tile.STATE_ACTIVE
        }

        setTileState(
            state = tileState,
            label = ldacLabelProvider.getLabel(),
            subtitle = ldacLabelProvider.getSubtitle(state, hasPermission, isConnected),
            icon = ldacIconProvider.getIcon(state, isConnected)
        )
    }
}