package com.wstxda.toolkit.services.hardware

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import com.wstxda.toolkit.utils.Haptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class MorseCodeFlasher(private val context: Context) {

    companion object {
        private const val TAG = "MorseCodeFlasher"
    }

    private val haptics by lazy { Haptics(context) }
    private val cameraManager by lazy { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private val cameraId: String? by lazy {
        try {
            val ids = cameraManager.cameraIdList
            ids.find { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash =
                    characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facingBack =
                    characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
                hasFlash && facingBack
            }
        } catch (e: Exception) {
            Log.w(TAG, "No camera available or failed to get cameraId", e)
            null
        }
    }

    private val sosScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var sosJob: Job? = null
    private val unitDuration = 200L

    fun startFlasher() {
        Log.i(TAG, "Start flasher")
        if (cameraId == null) {
            Log.w(TAG, "Cannot start flasher: no cameraId")
            return
        }

        sosJob?.cancel()
        sosJob = sosScope.launch {
            try {
                while (isActive) {
                    sendS()
                    delay(unitDuration * 3)
                    sendO()
                    delay(unitDuration * 3)
                    sendS()
                    delay(unitDuration * 7)
                }
            } catch (_: CancellationException) {
                Log.d(TAG, "Flasher cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Flasher failed", e)
            }
        }
    }

    fun stopFlasher() {
        Log.i(TAG, "Stop flasher")
        sosJob?.cancel()
        sosJob = null
        setTorchMode(false)
        haptics.cancel()
    }

    fun destroyService() {
        Log.i(TAG, "Destroy service")
        sosScope.cancel()
        stopFlasher()
    }

    val isRunning: Boolean
        get() = sosJob?.isActive == true

    private suspend fun sendS() {
        repeat(3) { dot() }
    }

    private suspend fun sendO() {
        repeat(3) { dash() }
    }

    private suspend fun dot() {
        blink(unitDuration)
        delay(unitDuration)
    }

    private suspend fun dash() {
        blink(unitDuration * 3)
        delay(unitDuration)
    }

    private suspend fun blink(duration: Long) {
        setTorchMode(true)
        haptics.morse(duration)
        delay(duration)
        setTorchMode(false)
    }

    private fun setTorchMode(enabled: Boolean) {
        Log.v(TAG, "Set torch mode: $enabled")
        cameraId?.let { id ->
            try {
                cameraManager.setTorchMode(id, enabled)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set torch mode", e)
            }
        }
    }
}