package com.wstxda.toolkit.activity

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.volume.VolumeControlDispatcher

class VolumeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLaunched = VolumeControlDispatcher.openVolumeControl(this)

        if (!isLaunched) {
            Toast.makeText(this, getString(R.string.not_supported), Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}