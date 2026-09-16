package com.wstxda.toolkit.tiles.ldac

import android.content.Intent
import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.activity.BluetoothPermissionActivity
import com.wstxda.toolkit.activity.LdacAssociationActivity
import com.wstxda.toolkit.activity.WriteSecureSettingsActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.ldac.LdacConnection
import com.wstxda.toolkit.manager.ldac.LdacModule
import com.wstxda.toolkit.manager.ldac.LdacSnapshot
import com.wstxda.toolkit.ui.icon.LdacIconProvider
import com.wstxda.toolkit.ui.label.LdacLabelProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class LdacTileService : BaseTileService() {

    private val manager by lazy { LdacModule.getInstance(applicationContext) }
    private val labelProvider by lazy { LdacLabelProvider(applicationContext) }
    private val iconProvider by lazy { LdacIconProvider(applicationContext) }
    private var pendingClickJob: Job? = null

    override fun onStartListening() {
        manager.startMonitoring()
        super.onStartListening()
    }

    override fun onStopListening() {
        super.onStopListening()
        manager.stopMonitoring()
    }

    override fun onDestroy() {
        manager.release()
        super.onDestroy()
    }

    override fun onClick() {
        manager.refresh()

        if (!manager.hasSecureSettingsPermission()) {
            startActivityAndCollapse(WriteSecureSettingsActivity::class.java)
            return
        }
        if (!manager.hasBluetoothPermission()) {
            startActivityAndCollapse(BluetoothPermissionActivity::class.java)
            return
        }

        pendingClickJob?.cancel()
        val snapshot = manager.snapshot.value
        if (snapshot.connection == LdacConnection.Connecting) {
            pendingClickJob = serviceScope.launch {
                val readySnapshot = withTimeoutOrNull(PROFILE_CONNECTION_TIMEOUT_MS) {
                    manager.snapshot.first { it.connection != LdacConnection.Connecting }
                } ?: manager.snapshot.value
                handleClick(readySnapshot)
            }
        } else {
            handleClick(snapshot)
        }
    }

    private fun handleClick(snapshot: LdacSnapshot) {
        when (snapshot.connection) {
            LdacConnection.PermissionRequired -> {
                startActivityAndCollapse(BluetoothPermissionActivity::class.java)
                return
            }
            LdacConnection.Connecting,
            LdacConnection.Disconnected -> {
                Toast.makeText(this, R.string.ldac_not_connected, Toast.LENGTH_SHORT).show()
                return
            }
            LdacConnection.NonLdac -> {
                Toast.makeText(this, R.string.ldac_not_active, Toast.LENGTH_SHORT).show()
                return
            }
            LdacConnection.Ready -> Unit
        }

        if (!manager.hasCompanionAssociation(snapshot.deviceAddress)) {
            launchActivityAndCollapse(
                Intent(this, LdacAssociationActivity::class.java).putExtra(
                    LdacAssociationActivity.EXTRA_DEVICE_ADDRESS, snapshot.deviceAddress
                )
            )
            return
        }

        if (!manager.cycleQuality()) {
            Toast.makeText(this, R.string.ldac_change_failed, Toast.LENGTH_SHORT).show()
        }
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(manager.snapshot)

    override fun updateTile() {
        val snapshot = manager.snapshot.value
        val hasSecureSettings = manager.hasSecureSettingsPermission()
        val hasAssociation = manager.hasCompanionAssociation(snapshot.deviceAddress)
        val available = hasSecureSettings &&
            snapshot.connection == LdacConnection.Ready &&
            hasAssociation

        setTileState(
            state = if (available) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = getString(R.string.ldac_tile),
            subtitle = labelProvider.getSubtitle(
                snapshot, hasSecureSettings, hasAssociation
            ),
            icon = iconProvider.getIcon(snapshot),
        )
    }

    companion object {
        private const val PROFILE_CONNECTION_TIMEOUT_MS = 2_000L
    }
}
