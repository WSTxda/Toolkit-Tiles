package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.system.MemoryState

class MemoryIconProvider(private val context: Context) {

    fun getIcon(state: MemoryState): Icon {
        val resId = when (state) {
            MemoryState.DISABLED -> R.drawable.ic_memory
            MemoryState.RAM -> R.drawable.ic_ram
            MemoryState.STORAGE -> R.drawable.ic_storage
        }
        return Icon.createWithResource(context, resId)
    }
}