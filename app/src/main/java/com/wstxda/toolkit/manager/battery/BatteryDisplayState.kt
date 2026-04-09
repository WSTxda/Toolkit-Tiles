package com.wstxda.toolkit.manager.battery

enum class BatteryDisplayState {
    PERCENTAGE, CURRENT, VOLTAGE, WATTAGE, TEMPERATURE;

    fun next(): BatteryDisplayState = entries[(ordinal + 1) % entries.size]
}