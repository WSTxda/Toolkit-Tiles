package com.wstxda.toolkit.manager.temperature

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT;

    fun next(): TemperatureUnit = when (this) {
        CELSIUS -> FAHRENHEIT
        FAHRENHEIT -> CELSIUS
    }
}
