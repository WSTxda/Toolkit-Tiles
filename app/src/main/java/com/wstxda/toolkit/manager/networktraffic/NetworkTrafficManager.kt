package com.wstxda.toolkit.manager.networktraffic

import android.content.Context
import android.net.TrafficStats
import androidx.core.content.edit
import com.wstxda.toolkit.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NetworkTrafficManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "network_traffic_prefs"
        private const val KEY_STATE = "current_state"
        private const val REFRESH_RATE_MS = 1000L
        private const val BYTES_IN_KB = 1024L
        private const val BYTES_IN_MB = 1024L * 1024L
    }

    private val appContext = context.applicationContext
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentState = MutableStateFlow(NetworkTrafficState.DOWNLOAD)
    val currentState = _currentState.asStateFlow()

    private val _speedValue = MutableStateFlow("")
    val speedValue = _speedValue.asStateFlow()

    private var pollingJob: Job? = null
    private var isPanelOpen = false

    private var lastRxBytes = TrafficStats.UNSUPPORTED.toLong()
    private var lastTxBytes = TrafficStats.UNSUPPORTED.toLong()
    private var lastSampleTime = 0L

    init {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedStateName = prefs.getString(KEY_STATE, NetworkTrafficState.DOWNLOAD.name)
        _currentState.value = runCatching {
            NetworkTrafficState.valueOf(savedStateName!!)
        }.getOrDefault(NetworkTrafficState.DOWNLOAD)
    }

    fun toggle() {
        val next = if (_currentState.value == NetworkTrafficState.DOWNLOAD) {
            NetworkTrafficState.UPLOAD
        } else {
            NetworkTrafficState.DOWNLOAD
        }
        _currentState.value = next
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_STATE, next.name)
        }
        restartPolling()
    }

    fun setListening(listening: Boolean) {
        if (isPanelOpen == listening) return
        isPanelOpen = listening
        if (isPanelOpen) restartPolling() else stopPolling()
    }

    private fun restartPolling() {
        pollingJob?.cancel()
        pollingJob = managerScope.launch {
            resetSamples()
            updateData()
            while (isActive) {
                delay(REFRESH_RATE_MS)
                updateData()
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        _speedValue.value = ""
    }

    private fun resetSamples() {
        val unsupported = TrafficStats.UNSUPPORTED.toLong()
        lastRxBytes = unsupported
        lastTxBytes = unsupported
        lastSampleTime = 0L
    }

    private fun updateData() {
        try {
            val now = System.currentTimeMillis()
            val rxBytes = TrafficStats.getTotalRxBytes()
            val txBytes = TrafficStats.getTotalTxBytes()
            val unsupported = TrafficStats.UNSUPPORTED.toLong()

            val isValidSample =
                lastSampleTime > 0 && rxBytes != unsupported && txBytes != unsupported

            val speedBytes: Long = if (isValidSample) {
                val elapsed = (now - lastSampleTime).coerceAtLeast(1L)
                val delta = when (_currentState.value) {
                    NetworkTrafficState.DOWNLOAD -> (rxBytes - lastRxBytes).coerceAtLeast(0L)
                    NetworkTrafficState.UPLOAD -> (txBytes - lastTxBytes).coerceAtLeast(0L)
                }
                (delta * 1000L) / elapsed
            } else {
                0L
            }

            lastRxBytes = rxBytes
            lastTxBytes = txBytes
            lastSampleTime = now

            _speedValue.value = formatSpeed(speedBytes)
        } catch (_: Exception) {
        }
    }

    private fun formatSpeed(bytesPerSecond: Long): String = when {
        bytesPerSecond >= BYTES_IN_MB -> appContext.getString(
            R.string.network_traffic_tile_mb, bytesPerSecond.toFloat() / BYTES_IN_MB
        )

        else -> appContext.getString(
            R.string.network_traffic_tile_kb, bytesPerSecond.toFloat() / BYTES_IN_KB
        )
    }
}