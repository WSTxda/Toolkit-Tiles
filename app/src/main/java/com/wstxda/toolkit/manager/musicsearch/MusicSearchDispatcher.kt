package com.wstxda.toolkit.manager.musicsearch

import android.content.Context
import android.content.Intent

object MusicSearchDispatcher {

    private const val GOOGLE_PACKAGE = "com.google.android.googlequicksearchbox"
    private const val MUSIC_SEARCH_ACTION = "$GOOGLE_PACKAGE.MUSIC_SEARCH"

    fun launchMusicSearch(context: Context): Boolean {
        return try {
            val intent = Intent(MUSIC_SEARCH_ACTION).apply {
                setPackage(GOOGLE_PACKAGE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}