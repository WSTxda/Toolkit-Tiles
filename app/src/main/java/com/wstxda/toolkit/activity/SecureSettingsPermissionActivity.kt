package com.wstxda.toolkit.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.wstxda.toolkit.R

class SecureSettingsPermissionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val command = "adb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"
        val message = getString(R.string.secure_settings_permission_message, packageName)

        AlertDialog.Builder(this)
            .setTitle(R.string.ldac_tile)
            .setMessage(message)
            .setPositiveButton(android.R.string.copy) { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("ADB Command", command)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                finish()
            }
            .setOnDismissListener {
                finish()
            }
            .show()
    }
}