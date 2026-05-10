package com.wstxda.toolkit.manager.clipboard

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

object ClipboardDispatcher {

    fun clearClipboard(context: Context): Boolean {
        return try {
            val clipboardManager = context.getSystemService<ClipboardManager>() ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboardManager.clearPrimaryClip()
            } else {
                clipboardManager.setPrimaryClip(
                    android.content.ClipData.newPlainText("", "")
                )
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}