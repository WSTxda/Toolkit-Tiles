package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R

class ClipboardLabelProvider(private val context: Context) {

    fun getLabel(): CharSequence {
        return context.getString(R.string.clipboard_tile)
    }

    fun getSubtitle(): CharSequence {
        return context.getString(R.string.clipboard_tile_tap)
    }
}