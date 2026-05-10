package com.wstxda.toolkit.activity

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.clipboard.ClipboardDispatcher

class ClipboardActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isCleared = ClipboardDispatcher.clearClipboard(this)

        Toast.makeText(
            this,
            if (isCleared) getString(R.string.clipboard_cleared) else getString(R.string.not_supported),
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }
}