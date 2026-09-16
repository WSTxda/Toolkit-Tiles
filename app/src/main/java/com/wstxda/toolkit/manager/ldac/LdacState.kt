package com.wstxda.toolkit.manager.ldac

enum class LdacState(val settingValue: Int) {
    Adaptive(1003),
    Connection(1002),
    Balanced(1001),
    Quality(1000);

    fun next(): LdacState = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromSetting(value: Int): LdacState =
            entries.firstOrNull { it.settingValue == value } ?: Adaptive
    }
}

data class LdacSnapshot(
    val quality: LdacState = LdacState.Adaptive,
    val connection: LdacConnection = LdacConnection.Disconnected,
    val deviceAddress: String? = null,
)

enum class LdacConnection {
    PermissionRequired,
    Connecting,
    Disconnected,
    NonLdac,
    Ready,
}
