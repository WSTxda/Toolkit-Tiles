package com.wstxda.toolkit.manager.caffeine

sealed class CaffeineState(val timeout: Int) {
    object Off : CaffeineState(-1)
    object FiveMinutes : CaffeineState(5 * 60 * 1000)
    object TenMinutes : CaffeineState(10 * 60 * 1000)
    object ThirtyMinutes : CaffeineState(30 * 60 * 1000)
    object OneHour : CaffeineState(60 * 60 * 1000)
    object Infinite : CaffeineState(24 * 60 * 60 * 1000)
}