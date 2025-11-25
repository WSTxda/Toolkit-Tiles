package com.wstxda.toolkit.tiles

import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.wstxda.toolkit.R
import com.wstxda.toolkit.services.hardware.MorseCodeFlasher
import com.wstxda.toolkit.utils.Haptics
import com.wstxda.toolkit.services.update

private const val TAG = "SosTileService"

class SosTileService : TileService() {

    private lateinit var sosService: MorseCodeFlasher
    private lateinit var cameraManager: CameraManager
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var haptics: Haptics

    private var isTorchOn = false
    private var isTorchAvailable = false
    private var cameraId: String? = null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            Log.i(TAG, "Torch mode changed to: $enabled for cameraId: $cameraId")
            if (this@SosTileService.cameraId == cameraId) {
                isTorchOn = enabled
                updateTileState()
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (this@SosTileService.cameraId == cameraId) {
                Log.i(TAG, "Torch mode unavailable for cameraId: $cameraId")
                isTorchAvailable = false
                updateTileState()
            }
        }
    }

    override fun onCreate() {
        Log.i(TAG, "Create")
        super.onCreate()
        haptics = Haptics(applicationContext)
        sosService = MorseCodeFlasher(this)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        try {
            val cameraIds = cameraManager.cameraIdList
            cameraId = cameraIds.find { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash =
                    characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facingBack =
                    characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
                hasFlash && facingBack
            }
            cameraId?.let {
                cameraManager.registerTorchCallback(torchCallback, handler)
                isTorchAvailable = true
            }
        } catch (_: Exception) {
            cameraId = null
            isTorchAvailable = false
        }
    }

    override fun onStartListening() {
        Log.i(TAG, "Start listening")
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        Log.i(TAG, "Click")
        super.onClick()
        if (qsTile.state == Tile.STATE_UNAVAILABLE) return

        if (!sosService.isRunning) {
            updateTileAsActive()
        } else {
            updateTileAsInactive()
        }
    }

    private fun updateTileAsActive() {
        sosService.startFlasher()
        updateTileState()
    }

    private fun updateTileAsInactive() {
        sosService.stopFlasher()
        updateTileState()
    }

    override fun onDestroy() {
        super.onDestroy()
        sosService.destroyService()
        cameraId?.let {
            cameraManager.unregisterTorchCallback(torchCallback)
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return

        val hasFlashHardware =
            packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) && cameraId != null

        if (!hasFlashHardware) {
            updateTileAsUnavailable(tile)
            return
        }
        if (!sosService.isRunning && (!isTorchAvailable || isTorchOn)) {
            updateTileAsUnavailable(tile)
            return
        }

        tile.update {
            state = if (sosService.isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            icon = Icon.createWithResource(this@SosTileService, R.drawable.ic_sos)
            label = getString(R.string.sos_tile_label)
        }
    }

    private fun updateTileAsUnavailable(tile: Tile) {
        tile.update {
            state = Tile.STATE_UNAVAILABLE
            icon = Icon.createWithResource(this@SosTileService, R.drawable.ic_sos)
            label = getString(R.string.sos_tile_label)
        }
    }
}