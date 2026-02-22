package com.wstxda.toolkit.manager.ldac

import android.Manifest
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothCodecConfig
import android.bluetooth.BluetoothCodecStatus
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.companion.CompanionDeviceManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.Method

/**
 * Manages LDAC codec playback quality for connected A2DP Bluetooth devices.
 *
 * Codec changes are applied through two mechanisms:
 * 1. [Settings.Global] write — persists the user preference
 * 2. [BluetoothA2dp.setCodecConfigPreference] via reflection — pushes to BT stack
 *
 * On Android 16+ (API 36), mechanism #2 requires a CDM association.
 * On Android 9–15, a VMRuntime hidden-API exemption is applied.
 * On Android 8.x, no exemption is needed.
 */
class LdacManager(context: Context) {

    companion object {
        private const val TAG = "LdacManager"
        private const val SETTING_KEY = "bluetooth_audio_ldac_codec_playback_quality"
        private const val DEFAULT_VALUE = 1003
        private const val USER_ACTION_LOCK_MS = 3000L

        /** CDM association is required starting from Android 16 (API 36). */
        private const val CDM_REQUIRED_API = 36
    }

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _currentState = MutableStateFlow(LdacState.ADAPTIVE)
    val currentState = _currentState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private var a2dpProxy: BluetoothA2dp? = null
    private val btAdapter: BluetoothAdapter? by lazy {
        appContext.getSystemService(BluetoothManager::class.java)?.adapter
    }

    private var setCodecMethod: Method? = null
    private var getCodecStatusMethod: Method? = null
    private var methodsResolved = false
    private var proxyConnected = false
    private var receiversRegistered = false

    @Volatile
    private var userLockUntil = 0L
    private var verifyJob: Job? = null

    init {
        applyHiddenApiExemption()
    }

    // region Hidden API exemption

    /**
     * On Android 9–15 (API 28–35), hidden API restrictions block reflection access
     * to BluetoothA2dp methods. VMRuntime.setHiddenApiExemptions bypasses this.
     * On Android 8.x, no restrictions exist. On Android 16+, the exemption no longer
     * works but methods resolve without it (CDM association gates invoke instead).
     */
    private fun applyHiddenApiExemption() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P ||
            Build.VERSION.SDK_INT >= CDM_REQUIRED_API
        ) return

        try {
            val vmRuntimeClass = Class.forName("dalvik.system.VMRuntime")
            val getRuntime = vmRuntimeClass.getMethod("getRuntime")
            val setExemptions = vmRuntimeClass.getMethod(
                "setHiddenApiExemptions", Array<String>::class.java
            )
            val runtime = getRuntime.invoke(null)
            setExemptions.invoke(runtime, arrayOf("L") as Any)
        } catch (_: Exception) {
            Log.w(TAG, "Hidden API exemption not applied")
        }
    }

    // endregion

    // region Bluetooth lifecycle

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile != BluetoothProfile.A2DP) return
            a2dpProxy = proxy as BluetoothA2dp
            proxyConnected = true
            refreshConnectionState()
            if (!methodsResolved) resolveCodecMethods()
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile != BluetoothProfile.A2DP) return
            a2dpProxy = null
            proxyConnected = false
            _isConnected.value = false
        }
    }

    private val btReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(
                        BluetoothProfile.EXTRA_STATE,
                        BluetoothProfile.STATE_DISCONNECTED
                    )
                    _isConnected.value = state == BluetoothProfile.STATE_CONNECTED
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
                        == BluetoothAdapter.STATE_OFF
                    ) {
                        _isConnected.value = false
                    }
                }
            }
        }
    }

    private val settingsObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            syncStateFromSettings()
        }
    }

    // endregion

    // region Monitoring

    fun startMonitoring() {
        if (!proxyConnected) {
            btAdapter?.getProfileProxy(appContext, profileListener, BluetoothProfile.A2DP)
        } else {
            refreshConnectionState()
        }

        if (!receiversRegistered) {
            receiversRegistered = true
            val filter = IntentFilter().apply {
                addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(btReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                appContext.registerReceiver(btReceiver, filter)
            }
            appContext.contentResolver.registerContentObserver(
                Settings.Global.getUriFor(SETTING_KEY), false, settingsObserver
            )
        }

        syncStateFromSettings()
    }

    fun stopMonitoring() {
        if (!receiversRegistered) return
        receiversRegistered = false
        runCatching { appContext.unregisterReceiver(btReceiver) }
        runCatching { appContext.contentResolver.unregisterContentObserver(settingsObserver) }
    }

    // endregion

    // region State reads

    private fun refreshConnectionState() {
        if (!hasBtPermission()) return
        val proxy = a2dpProxy ?: return
        _isConnected.value = runCatching { proxy.connectedDevices.isNotEmpty() }.getOrDefault(false)
    }

    private fun syncStateFromSettings() {
        if (System.currentTimeMillis() < userLockUntil) return
        val raw = Settings.Global.getInt(appContext.contentResolver, SETTING_KEY, DEFAULT_VALUE)
        _currentState.value = LdacState.fromValue(raw)
    }

    fun getConnectedDeviceAddress(): String? {
        if (!hasBtPermission()) return null
        return runCatching { a2dpProxy?.connectedDevices?.firstOrNull()?.address }.getOrNull()
    }

    // endregion

    // region Permission checks

    fun hasSecureSettingsPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.WRITE_SECURE_SETTINGS
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * On API 31+, checks BLUETOOTH_CONNECT runtime permission.
     * On API 26–30, legacy BLUETOOTH is an install-time permission — always granted.
     */
    fun hasBtPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns whether the currently connected device has a CDM association.
     * Always returns `true` on Android < 16 where CDM is not required.
     */
    fun hasCdmAssociation(): Boolean {
        if (Build.VERSION.SDK_INT < CDM_REQUIRED_API) return true

        val address = getConnectedDeviceAddress() ?: return false
        return runCatching {
            val cdm = appContext.getSystemService(CompanionDeviceManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                cdm.myAssociations.any { it.deviceMacAddress?.toString().equals(address, true) }
            } else {
                @Suppress("DEPRECATION")
                cdm.associations.any { it.equals(address, true) }
            }
        }.getOrDefault(false)
    }

    // endregion

    // region State mutation

    fun cycleState() {
        val entries = LdacState.entries
        val next = entries[(entries.indexOf(_currentState.value) + 1) % entries.size]

        userLockUntil = System.currentTimeMillis() + USER_ACTION_LOCK_MS
        _currentState.value = next

        Settings.Global.putInt(appContext.contentResolver, SETTING_KEY, next.value)

        verifyJob?.cancel()
        verifyJob = scope.launch(Dispatchers.IO) {
            applyToBluetoothStack(next)

            delay(USER_ACTION_LOCK_MS + 200)
            withContext(Dispatchers.Main) {
                userLockUntil = 0
                syncStateFromSettings()
            }
        }
    }

    // endregion

    // region Codec push via reflection

    private fun resolveCodecMethods() {
        methodsResolved = true
        try {
            val cls = Class.forName("android.bluetooth.BluetoothA2dp")
            getCodecStatusMethod = cls.getMethod("getCodecStatus", BluetoothDevice::class.java)
            setCodecMethod = cls.getMethod(
                "setCodecConfigPreference",
                BluetoothDevice::class.java,
                Class.forName("android.bluetooth.BluetoothCodecConfig")
            )
        } catch (e: Exception) {
            Log.w(TAG, "Codec methods unavailable: ${e.message}")
        }
    }

    private fun applyToBluetoothStack(state: LdacState) {
        val proxy = a2dpProxy ?: return
        val device = runCatching { proxy.connectedDevices.firstOrNull() }.getOrNull() ?: return
        val getStatus = getCodecStatusMethod ?: return
        val setPreference = setCodecMethod ?: return

        try {
            val status = getStatus.invoke(proxy, device) as? BluetoothCodecStatus ?: return
            val current = status.codecConfig ?: return
            val config = buildCodecConfig(current, state)
            setPreference.invoke(proxy, device, config)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            Log.w(TAG, "Codec push failed: ${e.cause?.message}")
        } catch (e: Exception) {
            Log.w(TAG, "Codec push failed: ${e.message}")
        }
    }

    /**
     * Builds a [BluetoothCodecConfig] with the updated LDAC quality value.
     * Uses [BluetoothCodecConfig.Builder] on API 33+ and the legacy
     * constructor via reflection on API 26–32 (it's package-private in the SDK).
     */
    private fun buildCodecConfig(
        current: BluetoothCodecConfig, state: LdacState
    ): BluetoothCodecConfig {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return BluetoothCodecConfig.Builder()
                .setCodecType(current.codecType)
                .setCodecPriority(BluetoothCodecConfig.CODEC_PRIORITY_HIGHEST)
                .setSampleRate(current.sampleRate)
                .setBitsPerSample(current.bitsPerSample)
                .setChannelMode(current.channelMode)
                .setCodecSpecific1(state.value.toLong())
                .build()
        }

        // Pre-API 33: constructor is package-private, use reflection
        val ctor = BluetoothCodecConfig::class.java.getDeclaredConstructor(
            Int::class.java, Int::class.java, Int::class.java,
            Int::class.java, Int::class.java,
            Long::class.java, Long::class.java, Long::class.java, Long::class.java
        ).apply { isAccessible = true }

        return ctor.newInstance(
            current.codecType,
            BluetoothCodecConfig.CODEC_PRIORITY_HIGHEST,
            current.sampleRate,
            current.bitsPerSample,
            current.channelMode,
            state.value.toLong(),
            0L, 0L, 0L
        )
    }

    // endregion
}