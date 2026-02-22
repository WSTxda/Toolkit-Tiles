package com.wstxda.toolkit.tiles.ldac

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.activity.CdmAssociationActivity
import com.wstxda.toolkit.activity.SecureSettingsPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.ldac.LdacModule
import com.wstxda.toolkit.ui.icon.LdacIconProvider
import com.wstxda.toolkit.ui.label.LdacLabelProvider
import kotlinx.coroutines.flow.Flow

class LdacTileService : BaseTileService() {

    private val ldacModule by lazy { LdacModule.getInstance(applicationContext) }
    private val labelProvider by lazy { LdacLabelProvider(applicationContext) }
    private val iconProvider by lazy { LdacIconProvider(applicationContext) }


    override fun onStartListening() {
        super.onStartListening()
        ldacModule.startMonitoring()
    }

    override fun onStopListening() {
        ldacModule.stopMonitoring()
        super.onStopListening()
    }

    override fun onClick() {
        if (!ldacModule.hasSecureSettingsPermission()) {
            startActivityAndCollapse(SecureSettingsPermissionActivity::class.java)
            return
        }

        if (!ldacModule.hasBtPermission()) {
            Toast.makeText(this, R.string.ldac_bluetooth_permission, Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivityAndCollapseCompat(intent)
            return
        }

        if (!ldacModule.isConnected.value) {
            Toast.makeText(this, R.string.ldac_not_connected, Toast.LENGTH_SHORT).show()
            return
        }

        if (!ldacModule.hasCdmAssociation()) {
            Toast.makeText(this, R.string.ldac_cdm_required, Toast.LENGTH_LONG).show()
            val intent = Intent(this, CdmAssociationActivity::class.java).apply {
                putExtra(CdmAssociationActivity.EXTRA_DEVICE_ADDRESS, ldacModule.getConnectedDeviceAddress())
            }
            startActivityAndCollapseCompat(intent)
            return
        }

        ldacModule.cycleState()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        ldacModule.currentState,
        ldacModule.isConnected
    )

    override fun updateTile() {
        val state = ldacModule.currentState.value
        val hasSecure = ldacModule.hasSecureSettingsPermission()
        val hasBt = ldacModule.hasBtPermission()
        val connected = ldacModule.isConnected.value
        val hasCdm = ldacModule.hasCdmAssociation()

        val tileState = when {
            !hasSecure || !hasBt || !connected || !hasCdm -> Tile.STATE_INACTIVE
            else -> Tile.STATE_ACTIVE
        }

        setTileState(
            state = tileState,
            label = labelProvider.getLabel(),
            subtitle = labelProvider.getSubtitle(state, hasSecure, hasBt, connected, hasCdm),
            icon = iconProvider.getIcon(state, connected)
        )
    }
}