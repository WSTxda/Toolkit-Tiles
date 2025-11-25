package com.wstxda.toolkit.tiles

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.nfc.NfcAdapter
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import com.wstxda.toolkit.R

private const val TAG = "NfcTileService"

class NfcTileService : TileService() {

    private var nfcAdapter: NfcAdapter? = null
    private var nfcStateReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        if (nfcAdapter == null) {
            updateTileAsUnavailable()
            return
        }
        updateTile()
        nfcStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
                    updateTile()
                }
            }
        }
        val filter = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
        registerReceiver(nfcStateReceiver, filter)
    }

    override fun onStopListening() {
        super.onStopListening()
        unregisterReceiver(nfcStateReceiver)
    }

    override fun onClick() {
        super.onClick()
        if (nfcAdapter == null) {
            showNotSupported()
            return
        }
        openNfcSettings()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        when (nfcAdapter?.adapterState) {
            NfcAdapter.STATE_ON -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = getString(R.string.nfc_tile_label)
                tile.icon = Icon.createWithResource(this, R.drawable.ic_nfc_on)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.tile_label_on)
                }
            }
            NfcAdapter.STATE_OFF -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.nfc_tile_label)
                tile.icon = Icon.createWithResource(this, R.drawable.ic_nfc_off)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.tile_label_off)
                }
            }
            else -> {
                // Intermediate states
                tile.state = Tile.STATE_UNAVAILABLE
                tile.label = getString(R.string.nfc_tile_label)
                tile.icon = Icon.createWithResource(this, R.drawable.ic_nfc_off)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.tile_label_off)
                }
            }
        }
        tile.updateTile()
    }

    private fun updateTileAsUnavailable() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_UNAVAILABLE
        tile.label = getString(R.string.nfc_tile_label)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_nfc_off)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = getString(R.string.not_supported)
        }
        tile.updateTile()
    }

    private fun showNotSupported() {
        Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show()
    }

    private fun openNfcSettings() {
        val intent = Intent(android.provider.Settings.ACTION_NFC_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityAndCollapse(intent)
    }
}
