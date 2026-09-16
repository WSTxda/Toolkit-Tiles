package com.wstxda.toolkit.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.service.quicksettings.TileService
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.tiles.ldac.LdacTileService

/** Requests the per-device association required by Android 16's codec-control API. */
@SuppressLint("NewApi")
class LdacAssociationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return

        val address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS)
        if (address == null) {
            finishWith(R.string.ldac_not_connected)
            return
        }

        val request = AssociationRequest.Builder()
            .addDeviceFilter(BluetoothDeviceFilter.Builder().setAddress(address).build())
            .setSingleDevice(true)
            .build()

        getSystemService(CompanionDeviceManager::class.java).associate(
            request,
            mainExecutor,
            object : CompanionDeviceManager.Callback() {
                override fun onAssociationPending(intentSender: IntentSender) {
                    runCatching {
                        startIntentSenderForResult(
                            intentSender, REQUEST_ASSOCIATION, null, 0, 0, 0
                        )
                    }.onFailure { finishWith(R.string.ldac_association_failed) }
                }

                override fun onAssociationCreated(associationInfo: AssociationInfo) {
                    if (!isFinishing) finishWith(R.string.ldac_association_complete)
                }

                override fun onFailure(error: CharSequence?) {
                    finishWith(R.string.ldac_association_failed)
                }
            },
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ASSOCIATION) {
            finishWith(
                if (resultCode == RESULT_OK) R.string.ldac_association_complete
                else R.string.ldac_association_cancelled
            )
        }
    }

    private fun finishWith(message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        TileService.requestListeningState(
            this, ComponentName(this, LdacTileService::class.java)
        )
        finish()
    }

    companion object {
        const val EXTRA_DEVICE_ADDRESS = "device_address"
        private const val REQUEST_ASSOCIATION = 1
    }
}
