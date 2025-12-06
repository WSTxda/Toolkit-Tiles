package com.wstxda.toolkit.manager.memory

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MemoryManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "memory_prefs"
        private const val KEY_STATE = "current_state"
        private const val REFRESH_RATE_MS = 1000L
    }

    private val appContext = context.applicationContext
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val memoryInfo = ActivityManager.MemoryInfo()

    private val _currentState = MutableStateFlow(MemoryState.RAM)
    val currentState = _currentState.asStateFlow()

    private val _usedValue = MutableStateFlow("")
    val usedValue = _usedValue.asStateFlow()

    private val _totalValue = MutableStateFlow("")
    val totalValue = _totalValue.asStateFlow()

    private val _detailValue = MutableStateFlow("")
    val detailValue = _detailValue.asStateFlow()

    private var pollingJob: Job? = null
    private var isPanelOpen = false

    init {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedStateName = prefs.getString(KEY_STATE, MemoryState.RAM.name)
        val savedState = runCatching { MemoryState.valueOf(savedStateName!!) }.getOrNull()
        _currentState.value = savedState ?: MemoryState.RAM
    }

    fun toggle() {
        val nextState =
            if (_currentState.value == MemoryState.RAM) MemoryState.STORAGE else MemoryState.RAM
        _currentState.value = nextState
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_STATE, nextState.name)
        }

        updateData()
    }

    fun setListening(listening: Boolean) {
        if (isPanelOpen == listening) return
        isPanelOpen = listening
        updatePollingState()
    }

    private fun updatePollingState() {
        if (isPanelOpen) {
            startPolling()
        } else {
            stopPolling()
        }
    }

    private fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = managerScope.launch {
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
    }

    private fun updateData() {
        try {
            when (_currentState.value) {
                MemoryState.RAM -> updateRamInfo()
                MemoryState.STORAGE -> updateStorageInfo()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateRamInfo() {
        activityManager.getMemoryInfo(memoryInfo)

        val totalBytes = memoryInfo.totalMem
        val usedBytes = totalBytes - memoryInfo.availMem
        val percent = ((usedBytes * 100) / totalBytes).toInt()

        _usedValue.value = Formatter.formatShortFileSize(appContext, usedBytes)
        _totalValue.value = Formatter.formatShortFileSize(appContext, totalBytes)
        _detailValue.value = "$percent%"
    }

    private fun updateStorageInfo() {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)

        val totalBytes = stat.blockCountLong * stat.blockSizeLong
        val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
        val usedBytes = totalBytes - freeBytes

        _usedValue.value = Formatter.formatShortFileSize(appContext, usedBytes)
        _totalValue.value = Formatter.formatShortFileSize(appContext, totalBytes)
        _detailValue.value = Formatter.formatShortFileSize(appContext, freeBytes)
    }
}