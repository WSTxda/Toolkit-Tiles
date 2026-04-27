package com.wstxda.toolkit.manager.sos

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SosManager(context: Context) {

    private val flasher = SosFlasher(context)
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    val isFlashAvailable: StateFlow<Boolean> = combine(
        flasher.isTorchAvailable, flasher.isTorchOn, _isActive
    ) { available, isOn, active ->
        if (active) {
            available
        } else {
            available && !isOn
        }
    }.stateIn(
        scope = managerScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = flasher.isTorchAvailable.value
    )

    fun hasFlashHardware(): Boolean = flasher.hasFlashHardware

    fun toggle() {
        if (!hasFlashHardware()) return
        if (!_isActive.value && !isFlashAvailable.value) return

        if (_isActive.value) {
            stopInternal()
        } else {
            startInternal()
        }
    }

    fun cleanup() {
        stopInternal()
        flasher.cleanup()
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