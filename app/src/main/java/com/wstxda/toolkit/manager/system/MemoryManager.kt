package com.wstxda.toolkit.manager.system

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

    private val _currentState = MutableStateFlow(MemoryState.DISABLED)
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
        val savedState = prefs.getString(KEY_STATE, MemoryState.DISABLED.name)
        _currentState.value = runCatching {
            MemoryState.valueOf(savedState!!)
        }.getOrDefault(MemoryState.DISABLED)
    }

    fun toggle() {
        val nextState = when (_currentState.value) {
            MemoryState.DISABLED -> MemoryState.RAM
            MemoryState.RAM -> MemoryState.STORAGE
            MemoryState.STORAGE -> MemoryState.DISABLED
        }

        _currentState.value = nextState

        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_STATE, nextState.name)
        }

        if (nextState != MemoryState.DISABLED) {
            updateData()
        } else {
            clearData()
        }

        updatePollingState()
    }

    fun setListening(listening: Boolean) {
        isPanelOpen = listening
        updatePollingState()
    }

    private fun updatePollingState() {
        val shouldPoll = _currentState.value != MemoryState.DISABLED && isPanelOpen

        if (shouldPoll) {
            if (pollingJob?.isActive != true) startPolling()
        } else {
            stopPolling()
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
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

    private fun clearData() {
        _usedValue.value = ""
        _totalValue.value = ""
        _detailValue.value = ""
    }

    private fun updateData() {
        when (_currentState.value) {
            MemoryState.RAM -> updateRamInfo()
            MemoryState.STORAGE -> updateStorageInfo()
            MemoryState.DISABLED -> {}
        }
    }

    private fun updateRamInfo() {
        val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)

        val usedBytes = memInfo.totalMem - memInfo.availMem
        val totalBytes = memInfo.totalMem
        val percent = (usedBytes.toDouble() / totalBytes.toDouble() * 100).toInt()

        _usedValue.value = Formatter.formatShortFileSize(appContext, usedBytes)
        _totalValue.value = Formatter.formatShortFileSize(appContext, totalBytes)
        _detailValue.value = "$percent%"
    }

    private fun updateStorageInfo() {
        try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)

            val totalBytes = stat.blockCountLong * stat.blockSizeLong
            val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
            val usedBytes = totalBytes - freeBytes

            _usedValue.value = Formatter.formatShortFileSize(appContext, usedBytes)
            _totalValue.value = Formatter.formatShortFileSize(appContext, totalBytes)
            _detailValue.value = Formatter.formatShortFileSize(appContext, freeBytes)
        } catch (_: Exception) {
            clearData()
        }
    }
}