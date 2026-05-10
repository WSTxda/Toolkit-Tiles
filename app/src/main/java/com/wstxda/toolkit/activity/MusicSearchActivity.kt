package com.wstxda.toolkit.activity

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.musicsearch.MusicSearchDispatcher

class MusicSearchActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLaunched = MusicSearchDispatcher.launchMusicSearch(this)

        if (!isLaunched) {
            Toast.makeText(
                this, getString(R.string.google_not_found), Toast.LENGTH_SHORT
            ).show()
        }

        finish()
    }
}