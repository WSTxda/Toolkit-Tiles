package com.wstxda.toolkit.manager.ldac

import android.Manifest
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.reflect.Method

class LdacManager(context: Context) {

    companion object {
        const val SETTING_KEY = "bluetooth_audio_ldac_codec_playback_quality"
        const val DEFAULT_VALUE = 1003
        private const val TAG = "LdacManager"
        private const val ACTION_LOCK_MS = 1500L
    }

    private val appContext = context.applicationContext
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    private val _currentState = MutableStateFlow<LdacState>(LdacState.ADAPTIVE)
    val currentState = _currentState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private var bluetoothA2dp: BluetoothA2dp? = null
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        appContext.getSystemService(BluetoothManager::class.java)?.adapter
    }

    private var getActiveDeviceMethod: Method? = null
    private var getCodecStatusMethod: Method? = null
    private var setCodecConfigPreferenceMethod: Method? = null

    private var lastUserActionTime = 0L

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            safeSynchronize()
        }
    }

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2dp = proxy as BluetoothA2dp
                managerScope.launch(Dispatchers.IO) {
                    try {
                        val a2dpClass = bluetoothA2dp!!.javaClass
                        getActiveDeviceMethod = a2dpClass.getDeclaredMethod("getActiveDevice").apply { isAccessible = true }
                        getCodecStatusMethod = a2dpClass.getDeclaredMethod("getCodecStatus", BluetoothDevice::class.java).apply { isAccessible = true }
                        
                        val codecConfigClass = Class.forName("android.bluetooth.BluetoothCodecConfig")
                        setCodecConfigPreferenceMethod = a2dpClass.getDeclaredMethod("setCodecConfigPreference", BluetoothDevice::class.java, codecConfigClass).apply { isAccessible = true }
                    } catch (_: Exception) {}
                    safeSynchronize()
                }
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2dp = null
                _isConnected.value = false
            }
        }
    }

    private val settingsObserver = object : android.database.ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            safeSynchronize()
        }
    }

    fun startMonitoring() {
        bluetoothAdapter?.getProfileProxy(appContext, profileListener, BluetoothProfile.A2DP)
        val filter = IntentFilter().apply {
            addAction("android.bluetooth.a2dp.profile.action.CODEC_CONFIG_CHANGED")
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        appContext.registerReceiver(bluetoothReceiver, filter, Context.RECEIVER_EXPORTED)
        appContext.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(SETTING_KEY),
            false,
            settingsObserver
        )
    }

    fun stopMonitoring() {
        bluetoothAdapter?.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2dp)
        try {
            appContext.unregisterReceiver(bluetoothReceiver)
            appContext.contentResolver.unregisterContentObserver(settingsObserver)
        } catch (_: Exception) {}
    }

    private fun getActiveDevice(a2dp: BluetoothA2dp): BluetoothDevice? {
        return try {
            getActiveDeviceMethod?.invoke(a2dp) as? BluetoothDevice ?: a2dp.connectedDevices.firstOrNull()
        } catch (_: Exception) { a2dp.connectedDevices.firstOrNull() }
    }

    private fun safeSynchronize() {
        if (System.currentTimeMillis() - lastUserActionTime < ACTION_LOCK_MS) return
        
        managerScope.launch(Dispatchers.IO) {
            val a2dp = bluetoothA2dp
            val device = a2dp?.let { getActiveDevice(it) }
            
            var newState: LdacState? = null
            
            if (a2dp != null && device != null && getCodecStatusMethod != null) {
                try {
                    val codecStatus = getCodecStatusMethod?.invoke(a2dp, device) as? BluetoothCodecStatus
                    codecStatus?.codecConfig?.let { config ->
                        val stackValue = config.codecSpecific1.toInt()
                        newState = LdacState.entries.find { it.value == stackValue }
                    }
                } catch (_: Exception) {}
            }

            if (newState == null) {
                val systemValue = Settings.Global.getInt(appContext.contentResolver, SETTING_KEY, DEFAULT_VALUE)
                newState = LdacState.entries.find { it.value == systemValue } ?: LdacState.ADAPTIVE
            }
            
            withContext(Dispatchers.Main) {
                _currentState.value = newState!!
                _isConnected.value = device != null
            }
        }
    }

    fun isPermissionGranted(): Boolean = 
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

    fun cycleState() {
        val stateCycle = LdacState.entries
        val nextState = stateCycle[(stateCycle.indexOf(_currentState.value) + 1) % stateCycle.size]
        
        lastUserActionTime = System.currentTimeMillis()
        _currentState.value = nextState
        
        managerScope.launch(Dispatchers.IO) {
            Settings.Global.putInt(appContext.contentResolver, SETTING_KEY, nextState.value)
            applyCodecPreference(nextState)
        }
    }

    private fun applyCodecPreference(state: LdacState) {
        val a2dp = bluetoothA2dp ?: return
        val device = getActiveDevice(a2dp) ?: return
        val setMethod = setCodecConfigPreferenceMethod ?: return

        try {
            val codecStatus = getCodecStatusMethod?.invoke(a2dp, device) as? BluetoothCodecStatus
            val currentConfig = codecStatus?.codecConfig ?: return

            val newConfig = BluetoothCodecConfig.Builder()
                .setCodecType(currentConfig.codecType)
                .setCodecPriority(currentConfig.codecPriority)
                .setSampleRate(currentConfig.sampleRate)
                .setBitsPerSample(currentConfig.bitsPerSample)
                .setChannelMode(currentConfig.channelMode)
                .setCodecSpecific1(state.value.toLong())
                .build()

            setMethod.invoke(a2dp, device, newConfig)
        } catch (_: Exception) {}
    }
}