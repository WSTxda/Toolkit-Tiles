package com.wstxda.toolkit.manager.tools

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SosManager(context: Context) {

    private val flasher = SosFlasher(context)
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    val isFlashAvailable = flasher.isTorchAvailable

    init {
        managerScope.launch {
            flasher.isTorchAvailable.collectLatest { available ->
                if (!available && _isActive.value) {
                    stopInternal()
                }
            }
        }
    }

    fun hasFlashHardware(): Boolean = flasher.hasFlashHardware

    fun toggle() {
        if (!hasFlashHardware()) return

        if (_isActive.value) {
            stopInternal()
        } else {
            startInternal()
        }
    }

    fun cleanup() {
        stopInternal()
        flasher.cleanup()
        managerScope.cancel()
    }

    private fun startInternal() {
        if (!flasher.hasFlashHardware) return
        _isActive.value = true
        flasher.start()
    }

    private fun stopInternal() {
        _isActive.value = false
        flasher.stop()
    }
}