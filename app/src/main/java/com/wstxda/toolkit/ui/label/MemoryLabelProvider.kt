package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.system.MemoryState

class MemoryLabelProvider(private val context: Context) {

    fun getLabel(state: MemoryState, detail: String): CharSequence {
        return when (state) {
            MemoryState.DISABLED -> context.getString(R.string.memory_tile)

            MemoryState.RAM -> {
                if (detail.isBlank()) return context.getString(R.string.memory_empty)
                context.getString(R.string.memory_ram, detail)
            }

            MemoryState.STORAGE -> {
                if (detail.isBlank()) return context.getString(R.string.memory_empty)
                context.getString(R.string.memory_storage, detail)
            }
        }
    }

    fun getSubtitle(state: MemoryState, used: String, total: String): CharSequence? {
        return when (state) {
            MemoryState.DISABLED -> context.getString(R.string.tile_switch)

            MemoryState.RAM, MemoryState.STORAGE -> {
                if (used.isBlank() || total.isBlank()) return null
                context.getString(R.string.memory_format, used, total)
            }
        }
    }
}