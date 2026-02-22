package com.wstxda.toolkit.activity

import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.wstxda.toolkit.R

/**
 * Handles Companion Device Manager (CDM) association for a Bluetooth device.
 *
 * When a device address is provided via [EXTRA_DEVICE_ADDRESS], the system shows
 * a simple consent dialog. Otherwise, it falls back to scanning for discoverable
 * devices (which may not find already-paired devices).
 *
 * The CDM association is required on Android 16+ for the app to push codec
 * changes to the Bluetooth stack via [BluetoothA2dp.setCodecConfigPreference].
 */
class CdmAssociationActivity : FragmentActivity() {

    companion object {
        const val EXTRA_DEVICE_ADDRESS = "device_address"
        private const val RC_ASSOCIATE = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) startAssociation()
    }

    private fun startAssociation() {
        val cdm = getSystemService(CompanionDeviceManager::class.java)
        val address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS)

        val filter = BluetoothDeviceFilter.Builder().apply {
            if (address != null) setAddress(address)
        }.build()

        val request = AssociationRequest.Builder()
            .addDeviceFilter(filter)
            .apply { if (address != null) setSingleDevice(true) }
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            cdm.associate(request, mainExecutor, object : CompanionDeviceManager.Callback() {
                override fun onAssociationPending(intentSender: IntentSender) {
                    runCatching {
                        startIntentSenderForResult(intentSender, RC_ASSOCIATE, null, 0, 0, 0)
                    }.onFailure { finishWith(R.string.ldac_cdm_failed) }
                }

                override fun onAssociationCreated(associationInfo: AssociationInfo) {
                    finishWith(R.string.ldac_cdm_success)
                }

                override fun onFailure(error: CharSequence?) {
                    finishWith(R.string.ldac_cdm_failed)
                }
            })
        } else {
            @Suppress("DEPRECATION")
            cdm.associate(request, object : CompanionDeviceManager.Callback() {
                @Deprecated("Deprecated in Java")
                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    runCatching {
                        startIntentSenderForResult(chooserLauncher, RC_ASSOCIATE, null, 0, 0, 0)
                    }.onFailure { finishWith(R.string.ldac_cdm_failed) }
                }

                override fun onFailure(error: CharSequence?) {
                    finishWith(R.string.ldac_cdm_failed)
                }
            }, null)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_ASSOCIATE) {
            finishWith(if (resultCode == RESULT_OK) R.string.ldac_cdm_success else R.string.ldac_cdm_cancelled)
        }
    }

    private fun finishWith(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
        finish()
    }
}
