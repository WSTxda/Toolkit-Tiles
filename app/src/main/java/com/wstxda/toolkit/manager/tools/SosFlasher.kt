package com.wstxda.toolkit.manager.tools

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.wstxda.toolkit.ui.utils.Haptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class SosFlasher(context: Context) {

    private val appContext = context.applicationContext
    private val haptics = Haptics(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val unit = 200L

    private val cameraManager by lazy {
        appContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val _isTorchOn = MutableStateFlow(false)
    private val _torchAvailable = MutableStateFlow(true)

    val isTorchAvailable = _torchAvailable.asStateFlow()

    private var sosJob: Job? = null

    private val cameraId: String? = try {
        cameraManager.cameraIdList.find { id ->
            val c = cameraManager.getCameraCharacteristics(id)
            val hasFlash = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            val isBack =
                c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            hasFlash && isBack
        }
    } catch (_: Exception) {
        null
    }

    val hasFlashHardware: Boolean get() = cameraId != null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(id: String, enabled: Boolean) {
            if (id == cameraId) {
                _isTorchOn.value = enabled
                _torchAvailable.value = true
            }
        }

        override fun onTorchModeUnavailable(id: String) {
            if (id == cameraId) {
                _torchAvailable.value = false
            }
        }
    }

    init {
        cameraId?.let {
            try {
                cameraManager.registerTorchCallback(torchCallback, null)
            } catch (_: Exception) {
            }
        }
    }

    fun start() {
        if (!hasFlashHardware || !_torchAvailable.value || sosJob?.isActive == true) return

        sosJob?.cancel()
        sosJob = scope.launch {
            try {
                while (isActive) {
                    delay(unit * 2)
                    sendS()
                    delay(unit * 2)
                    sendO()
                    delay(unit * 2)
                    sendS()
                    delay(unit * 4)
                }
            } finally {
                withContext(NonCancellable) {
                    setTorch(false)
                }
            }
        }
    }

    fun stop() {
        sosJob?.cancel()
        sosJob = null
        haptics.cancel()
    }

    fun cleanup() {
        stop()
        try {
            cameraManager.unregisterTorchCallback(torchCallback)
        } catch (_: Exception) {
        }
        scope.cancel()
    }

    private suspend fun sendS() = repeat(3) {
        if (coroutineContext.isActive) dot()
    }

    private suspend fun sendO() = repeat(3) {
        if (coroutineContext.isActive) dash()
    }

    private suspend fun dot() {
        blink(unit)
        delay(unit)
    }

    private suspend fun dash() {
        blink(unit * 3)
        delay(unit)
    }

    private suspend fun blink(duration: Long) {
        if (!coroutineContext.isActive) return
        setTorch(true)
        haptics.long(duration)
        delay(duration)
        setTorch(false)
    }

    private fun setTorch(enabled: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager.setTorchMode(id, enabled)
        } catch (_: CameraAccessException) {
            _torchAvailable.value = false
        } catch (_: Exception) {
        }
    }
}