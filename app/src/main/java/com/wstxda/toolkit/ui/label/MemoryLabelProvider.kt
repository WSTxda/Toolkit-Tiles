package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.memory.MemoryState

class MemoryLabelProvider(private val context: Context) {

    fun getLabel(state: MemoryState, detail: String): CharSequence {
        if (detail.isBlank()) return context.getString(R.string.memory_tile)

        return when (state) {
            MemoryState.RAM -> context.getString(R.string.memory_tile_ram, detail)
            MemoryState.STORAGE -> context.getString(R.string.memory_tile_storage, detail)
        }
    }

    fun getSubtitle(used: String, total: String): CharSequence? {
        if (used.isBlank() || total.isBlank()) return null
        return context.getString(R.string.memory_tile_format, used, total)
    }
}