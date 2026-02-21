package com.wstxda.toolkit.tiles.ldac

import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.activity.SecureSettingsPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.ldac.LdacModule
import com.wstxda.toolkit.manager.ldac.LdacState
import com.wstxda.toolkit.ui.icon.LdacIconProvider
import com.wstxda.toolkit.ui.label.LdacLabelProvider
import kotlinx.coroutines.flow.Flow

class LdacTileService : BaseTileService() {

    private val ldacModule by lazy { LdacModule.getInstance(applicationContext) }
    private val ldacLabelProvider by lazy { LdacLabelProvider(applicationContext) }
    private val ldacIconProvider by lazy { LdacIconProvider(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        ldacModule.synchronizeState()
    }

    override fun onClick() {
        if (ldacModule.isPermissionGranted()) {
            if (!ldacModule.cycleState()) {
                Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show()
            }
        } else {
            startActivityAndCollapse(SecureSettingsPermissionActivity::class.java)
        }
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(ldacModule.currentState)
    }

    override fun updateTile() {
        val state = ldacModule.currentState.value
        val hasPermission = ldacModule.isPermissionGranted()

        setTileState(
            state = if (hasPermission) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = ldacLabelProvider.getLabel(),
            subtitle = ldacLabelProvider.getSubtitle(state, hasPermission),
            icon = ldacIconProvider.getIcon(state)
        )
    }
}