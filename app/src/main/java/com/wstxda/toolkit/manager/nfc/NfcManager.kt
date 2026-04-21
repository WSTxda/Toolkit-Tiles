package com.wstxda.toolkit.manager.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.NfcManager as SystemNfcManager
import com.wstxda.toolkit.permissions.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NfcManager(context: Context) {

    private val appContext = context.applicationContext
    private val permissionManager = PermissionManager(appContext)
    private val systemNfcManager = appContext.getSystemService(Context.NFC_SERVICE) as? SystemNfcManager
    private val nfcAdapter: NfcAdapter? = systemNfcManager?.defaultAdapter
    val hasHardware: Boolean = nfcAdapter != null

    private val _isEnabled = MutableStateFlow(getCurrentSystemMode())
    val isEnabled = _isEnabled.asStateFlow()

    private var isListening = false

    private val receiverObserver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
                syncStateWithSystem()
            }
        }
    }

    private fun getCurrentSystemMode(): Boolean {
        if (nfcAdapter == null) return false
        return try {
            nfcAdapter.isEnabled
        } catch (_: Exception) {
            false
        }
    }

    private fun syncStateWithSystem() {
        if (!hasHardware) return
        val systemState = getCurrentSystemMode()
        if (_isEnabled.value != systemState) {
            _isEnabled.value = systemState
        }
    }

    fun start() {
        if (isListening || !hasHardware) return
        syncStateWithSystem()
        appContext.registerReceiver(
            receiverObserver, IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
        )
        isListening = true
    }

    fun stop() {
        if (!isListening) return
        try {
            appContext.unregisterReceiver(receiverObserver)
        } catch (_: Exception) {
        }
        isListening = false
    }

    fun cleanup() {
        stop()
    }

    fun hasPermission(): Boolean = permissionManager.hasWriteSecureSettingsPermission()

    fun toggle() {
        if (!hasHardware || !hasPermission()) return
        val newState = !_isEnabled.value

        if (setSystemMode(newState)) {
            _isEnabled.value = newState
        }
    }

    private fun setSystemMode(enable: Boolean): Boolean {
        if (nfcAdapter == null) return false
        return try {
            val method = nfcAdapter.javaClass.getDeclaredMethod(if (enable) "enable" else "disable")
            method.isAccessible = true
            val success = method.invoke(nfcAdapter) as? Boolean ?: true
            success
        } catch (_: Exception) {
            false
        }
    }
}