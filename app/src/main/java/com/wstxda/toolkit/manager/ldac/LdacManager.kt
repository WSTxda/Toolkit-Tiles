package com.wstxda.toolkit.manager.ldac

import android.Manifest
import android.annotation.SuppressLint
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
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.wstxda.toolkit.permissions.PermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.reflect.Method

/** Controls the platform LDAC playback-quality preference for the active A2DP device. */
@SuppressLint("InlinedApi", "MissingPermission", "NewApi")
class LdacManager(context: Context) {

    companion object {
        private const val TAG = "LdacManager"
        private const val QUALITY_SETTING = "bluetooth_audio_ldac_codec_playback_quality"
        private const val DEFAULT_QUALITY = 1003
        private const val LDAC_CODEC_TYPE = 4
        private const val CDM_REQUIRED_API = 36
        private const val ACTION_CODEC_CONFIG_CHANGED =
            "android.bluetooth.a2dp.profile.action.CODEC_CONFIG_CHANGED"
    }

    private val appContext = context.applicationContext
    private val permissionManager = PermissionManager(appContext)
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bluetoothAdapter by lazy {
        appContext.getSystemService(BluetoothManager::class.java)?.adapter
    }

    private val _snapshot = MutableStateFlow(LdacSnapshot())
    val snapshot = _snapshot.asStateFlow()

    @Volatile
    private var a2dp: BluetoothA2dp? = null
    private var proxyRequested = false
    private var monitoring = false

    private var getCodecStatus: Method? = null
    private var setCodecPreference: Method? = null

    init {
        exemptBluetoothHiddenApis()
        resolveCodecMethods()
    }

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile != BluetoothProfile.A2DP) return
            a2dp = proxy as BluetoothA2dp
            proxyRequested = false
            refreshSnapshot()
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile != BluetoothProfile.A2DP) return
            a2dp = null
            proxyRequested = false
            refreshSnapshot()
            if (monitoring) ensureProfileProxy()
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    ensureProfileProxy()
                    refreshSnapshot()
                }
                BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED,
                ACTION_CODEC_CONFIG_CHANGED -> refreshSnapshot()
            }
        }
    }

    private val qualityObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) = refreshQuality()
    }

    fun startMonitoring() {
        if (!monitoring) {
            val filter = IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
                addAction(ACTION_CODEC_CONFIG_CHANGED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(
                    bluetoothReceiver, filter, Context.RECEIVER_EXPORTED
                )
            } else {
                appContext.registerReceiver(bluetoothReceiver, filter)
            }
            appContext.contentResolver.registerContentObserver(
                Settings.Global.getUriFor(QUALITY_SETTING), false, qualityObserver
            )
            monitoring = true
        }

        ensureProfileProxy()
        refreshSnapshot()
    }

    fun stopMonitoring() {
        if (!monitoring) return
        runCatching { appContext.unregisterReceiver(bluetoothReceiver) }
        runCatching { appContext.contentResolver.unregisterContentObserver(qualityObserver) }
        monitoring = false
    }

    fun release() {
        stopMonitoring()
        val proxy = a2dp
        if (proxy != null && hasBluetoothPermission()) {
            runCatching { bluetoothAdapter?.closeProfileProxy(BluetoothProfile.A2DP, proxy) }
        }
        a2dp = null
        proxyRequested = false
    }

    fun hasSecureSettingsPermission(): Boolean =
        permissionManager.hasWriteSecureSettingsPermission()

    fun hasBluetoothPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            appContext.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED

    fun hasCompanionAssociation(address: String?): Boolean {
        if (Build.VERSION.SDK_INT < CDM_REQUIRED_API) return true
        if (address == null || !hasBluetoothPermission()) return false

        return runCatching {
            appContext.getSystemService(CompanionDeviceManager::class.java)
                .myAssociations
                .any { it.deviceMacAddress?.toString().equals(address, ignoreCase = true) }
        }.getOrDefault(false)
    }

    fun refresh() {
        ensureProfileProxy()
        refreshSnapshot()
    }

    fun cycleQuality(): Boolean {
        if (!hasSecureSettingsPermission()) return false
        val current = _snapshot.value
        if (current.connection != LdacConnection.Ready) return false

        if (!hasCompanionAssociation(current.deviceAddress)) return false

        val next = current.quality.next()
        _snapshot.value = current.copy(quality = next)
        managerScope.launch {
            val saved = runCatching {
                Settings.Global.putInt(
                    appContext.contentResolver, QUALITY_SETTING, next.settingValue
                )
            }.getOrDefault(false)

            if (saved) {
                applyQualityToStack(next, current.deviceAddress)
            } else {
                refreshQuality()
            }
        }
        return true
    }

    private fun ensureProfileProxy() {
        if (!hasBluetoothPermission()) {
            _snapshot.value = readSnapshot(LdacConnection.PermissionRequired)
            return
        }
        if (a2dp != null || proxyRequested) return

        _snapshot.value = readSnapshot(LdacConnection.Connecting)
        proxyRequested = runCatching {
            bluetoothAdapter?.getProfileProxy(
                appContext, profileListener, BluetoothProfile.A2DP
            ) == true
        }.getOrDefault(false)
        if (!proxyRequested) {
            _snapshot.value = readSnapshot(LdacConnection.Disconnected)
        }
    }

    private fun refreshQuality() {
        _snapshot.value = _snapshot.value.copy(quality = readQuality())
    }

    private fun refreshSnapshot() {
        if (!hasBluetoothPermission()) {
            _snapshot.value = readSnapshot(LdacConnection.PermissionRequired)
            return
        }

        val proxy = a2dp
        if (proxy == null) {
            _snapshot.value = readSnapshot(
                if (proxyRequested) LdacConnection.Connecting else LdacConnection.Disconnected
            )
            return
        }

        val devices = runCatching { proxy.connectedDevices }.getOrDefault(emptyList())
        if (devices.isEmpty()) {
            _snapshot.value = readSnapshot(LdacConnection.Disconnected)
            return
        }

        val ldacDevice = devices.firstOrNull(::isUsingLdac)
        _snapshot.value = if (ldacDevice == null) {
            readSnapshot(LdacConnection.NonLdac)
        } else {
            readSnapshot(LdacConnection.Ready, ldacDevice.address)
        }
    }

    private fun readSnapshot(
        connection: LdacConnection,
        address: String? = null,
    ) = LdacSnapshot(readQuality(), connection, address)

    private fun readQuality(): LdacState = LdacState.fromSetting(
        Settings.Global.getInt(appContext.contentResolver, QUALITY_SETTING, DEFAULT_QUALITY)
    )

    private fun isUsingLdac(device: BluetoothDevice): Boolean = runCatching {
        codecStatus(device)?.codecConfig?.codecType == LDAC_CODEC_TYPE
    }.getOrDefault(false)

    private fun codecStatus(device: BluetoothDevice): BluetoothCodecStatus? {
        val proxy = a2dp ?: return null
        return getCodecStatus?.invoke(proxy, device) as? BluetoothCodecStatus
    }

    private fun applyQualityToStack(quality: LdacState, address: String?) {
        val proxy = a2dp ?: return
        val method = setCodecPreference ?: return
        val device = runCatching {
            proxy.connectedDevices.firstOrNull {
                it.address.equals(address, ignoreCase = true)
            }
        }.getOrNull() ?: return

        runCatching {
            val current = codecStatus(device)?.codecConfig ?: return@runCatching
            if (current.codecType != LDAC_CODEC_TYPE) return@runCatching
            method.invoke(proxy, device, current.withLdacQuality(quality))
        }.onFailure { error ->
            Log.w(TAG, "Unable to apply LDAC quality", error.cause ?: error)
        }
    }

    private fun BluetoothCodecConfig.withLdacQuality(quality: LdacState): BluetoothCodecConfig {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return BluetoothCodecConfig.Builder()
                .setCodecType(codecType)
                .setCodecPriority(BluetoothCodecConfig.CODEC_PRIORITY_HIGHEST)
                .setSampleRate(sampleRate)
                .setBitsPerSample(bitsPerSample)
                .setChannelMode(channelMode)
                .setCodecSpecific1(quality.settingValue.toLong())
                .setCodecSpecific2(codecSpecific2)
                .setCodecSpecific3(codecSpecific3)
                .setCodecSpecific4(codecSpecific4)
                .build()
        }

        val constructor = BluetoothCodecConfig::class.java.getDeclaredConstructor(
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Long::class.javaPrimitiveType,
            Long::class.javaPrimitiveType,
            Long::class.javaPrimitiveType,
            Long::class.javaPrimitiveType,
        ).apply { isAccessible = true }

        return constructor.newInstance(
            codecType,
            BluetoothCodecConfig.CODEC_PRIORITY_HIGHEST,
            sampleRate,
            bitsPerSample,
            channelMode,
            quality.settingValue.toLong(),
            codecSpecific2,
            codecSpecific3,
            codecSpecific4,
        )
    }

    private fun resolveCodecMethods() {
        runCatching {
            getCodecStatus = BluetoothA2dp::class.java.getMethod(
                "getCodecStatus", BluetoothDevice::class.java
            )
            setCodecPreference = BluetoothA2dp::class.java.getMethod(
                "setCodecConfigPreference",
                BluetoothDevice::class.java,
                BluetoothCodecConfig::class.java,
            )
        }.onFailure { Log.w(TAG, "Bluetooth codec APIs unavailable", it) }
    }

    private fun exemptBluetoothHiddenApis() {
        if (Build.VERSION.SDK_INT !in Build.VERSION_CODES.P until CDM_REQUIRED_API) return

        runCatching {
            val vmRuntime = Class.forName("dalvik.system.VMRuntime")
            val runtime = vmRuntime.getMethod("getRuntime").invoke(null)
            vmRuntime.getMethod(
                "setHiddenApiExemptions", Array<String>::class.java
            ).invoke(
                runtime,
                arrayOf(
                    "Landroid/bluetooth/BluetoothA2dp;",
                    "Landroid/bluetooth/BluetoothCodecConfig;",
                ),
            )
        }.onFailure { Log.w(TAG, "Hidden Bluetooth APIs unavailable", it) }
    }
}
