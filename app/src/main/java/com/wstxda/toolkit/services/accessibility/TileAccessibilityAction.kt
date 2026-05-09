package com.wstxda.toolkit.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Build
import androidx.annotation.RequiresApi

object TileAccessibilityAction {

    @RequiresApi(Build.VERSION_CODES.P)
    const val LOCK_SCREEN = AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN

    @RequiresApi(Build.VERSION_CODES.P)
    const val TAKE_SCREENSHOT = AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT

    @RequiresApi(Build.VERSION_CODES.P)
    const val POWER_DIALOG = AccessibilityService.GLOBAL_ACTION_POWER_DIALOG
}