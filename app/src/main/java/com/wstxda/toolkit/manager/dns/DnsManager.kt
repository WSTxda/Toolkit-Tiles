package com.wstxda.toolkit.manager.dns

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.wstxda.toolkit.permissions.PermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

class DnsManager(private val context: Context) {

    companion object {
        private const val PRIVATE_DNS_MODE = "private_dns_mode"
        private const val PRIVATE_DNS_SPECIFIER = "private_dns_specifier"
        private const val MODE_OFF = "off"
        private const val MODE_AUTO = "opportunistic"
        private const val MODE_HOSTNAME = "hostname"
    }

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val permissionManager = PermissionManager(context.applicationContext)

    val currentProvider: StateFlow<DnsProvider> = callbackFlow {
        trySend(getCurrentProviderInternal())

        val handler = Handler(Looper.getMainLooper())
        val observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                trySend(getCurrentProviderInternal())
            }
        }

        val resolver = context.contentResolver
        resolver.registerContentObserver(
            Settings.Global.getUriFor(PRIVATE_DNS_MODE), false, observer
        )
        resolver.registerContentObserver(
            Settings.Global.getUriFor(PRIVATE_DNS_SPECIFIER), false, observer
        )

        awaitClose {
            resolver.unregisterContentObserver(observer)
        }
    }.distinctUntilChanged().stateIn(
        scope = managerScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = getCurrentProviderInternal(),
    )

    fun hasPermission(): Boolean = permissionManager.hasWriteSecureSettingsPermission()

    fun cycleProvider() {
        val current = getCurrentProviderInternal()
        val providers = DnsProvider.entries
        val next = providers[(current.ordinal + 1) % providers.size]
        applyProvider(next)
    }

    fun getCurrentProviderInternal(): DnsProvider {
        val mode = Settings.Global.getString(context.contentResolver, PRIVATE_DNS_MODE)
            ?: return DnsProvider.AUTOMATIC
        val hostname =
            Settings.Global.getString(context.contentResolver, PRIVATE_DNS_SPECIFIER) ?: ""

        return when (mode) {
            MODE_OFF -> DnsProvider.AUTOMATIC
            MODE_AUTO -> DnsProvider.AUTOMATIC
            MODE_HOSTNAME -> DnsProvider.entries.firstOrNull { it.hostname == hostname }
                ?: DnsProvider.AUTOMATIC

            else -> DnsProvider.AUTOMATIC
        }
    }

    private fun applyProvider(provider: DnsProvider) {
        try {
            if (provider == DnsProvider.AUTOMATIC) {
                Settings.Global.putString(context.contentResolver, PRIVATE_DNS_MODE, MODE_AUTO)
            } else {
                Settings.Global.putString(
                    context.contentResolver,
                    PRIVATE_DNS_SPECIFIER,
                    provider.hostname,
                )
                Settings.Global.putString(context.contentResolver, PRIVATE_DNS_MODE, MODE_HOSTNAME)
            }
        } catch (_: SecurityException) {
        }
    }

    fun cleanup() {
        managerScope.cancel()
    }
}